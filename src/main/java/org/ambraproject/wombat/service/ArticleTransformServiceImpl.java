package org.ambraproject.wombat.service;

import com.google.common.collect.ImmutableSet;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.controller.DoiVersionArgumentResolver;
import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.model.Reference;
import org.ambraproject.wombat.model.References;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.WriterOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

public class ArticleTransformServiceImpl implements ArticleTransformService {
  private static final Logger log = LoggerFactory.getLogger(ArticleTransformServiceImpl.class);

  private static final SiteTransformerFactory SITE_TRANSFORMER_FACTORY = new SiteTransformerFactory(
      "xform/", "article-transform.xsl");

  @Autowired
  private Charset charset;

  /*
   * JATS (Journal Archiving Tag Suite) is a continuation of the work to create and support the "NLM DTDs".
   * JATS is fully backward compatible with NLM version 3.0 and is being maintained by NISO as the NLM working
   * group was disbanded. You can read more at: http://jats.nlm.nih.gov/about.html
   * We decided to whitelist the valid dtds instead of letting everything through and log the exceptions later
   * in the workflow, because the list is short and it doesn't seem to grow often and it is better to prevent
   * bugs than looking for them.
   */
  private static final ImmutableSet<String> VALID_DTDS = ImmutableSet.of(
      "http://dtd.nlm.nih.gov/publishing/3.0/journalpublishing3.dtd",
      "http://jats.nlm.nih.gov/publishing/1.1d2/JATS-journalpublishing1.dtd",
      "http://jats.nlm.nih.gov/publishing/1.1d3/JATS-journalpublishing1.dtd");


  @FunctionalInterface
  private static interface TransformerInitializer {
    /**
     * Set up a {@link Transformer} to render a particular piece of article content.
     *
     * @param xmlReader   an {@link XMLReader} instance to use
     * @param transformer the {@code Transformer} object to modify
     * @throws IOException
     */
    void initialize(XMLReader xmlReader, Transformer transformer) throws IOException;
  }


  /**
   * Build a new transformer and attach any required parameters for the given render context
   *
   * @param site           The site on which the content will be rendered.
   * @param xmlReader      An XML reader instance which is provided to parse XML-formatted strings for the purpose of
   *                       creating any secondary SAX sources for the transform (passed to the transformer as
   *                       parameters)
   * @param initialization Hook in which information (such as the site object and articleId, and potentially other
   *                       variables related to client side capabilities or identifiers of specific excerpts targeted
   *                       for rendering) to be injected and passed to the transformer object.
   * @return the transformer
   * @throws IOException
   */
  private Transformer buildTransformer(Site site, XMLReader xmlReader, TransformerInitializer initialization)
      throws IOException {
    log.debug("Building transformer for: {}", site);
    Transformer transformer = SITE_TRANSFORMER_FACTORY.build(site);
    initialization.initialize(xmlReader, transformer);
    return transformer;
  }

  @Override
  public void transformArticle(Site site, ArticlePointer articleId, List<Reference> references,
                               InputStream xml, OutputStream html)
      throws IOException {
    boolean showsCitedArticles = (boolean) site.getTheme().getConfigMap("article").get("showsCitedArticles");
    transform(site, xml, html,
        (XMLReader xmlReader, Transformer transformer) -> {
          if (showsCitedArticles) {
            setCitedArticles(references, xmlReader, transformer);
          }

          setVersionLink(articleId, transformer);
        });
  }

  // Add cited articles metadata for inclusion of DOI links in reference list
  private void setCitedArticles(List<Reference> references, XMLReader xmlReader, Transformer transformer) throws IOException {
    References refs = new References();
    refs.setReferences(references);
    StringWriter sw = new StringWriter();
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(References.class);
      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
      jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      jaxbMarshaller.marshal(refs, sw);
    } catch (JAXBException jaxbE) {
      throw new RuntimeException(jaxbE);
    }

    String metadataXml = sw.toString();
    SAXSource saxSourceMeta = new SAXSource(xmlReader, new InputSource(IOUtils.toInputStream(metadataXml)));
    transformer.setParameter("refs", saxSourceMeta);
  }

  private void setVersionLink(ArticlePointer articleId, Transformer transformer) {
    final String versionLinkParameter;
    if (articleId.isOriginalRequestVersioned()) {
      final String versionType;
      final int versionNumber;

      OptionalInt revisionNumber = articleId.getRevisionNumber();
      if (revisionNumber.isPresent()) {
        versionType = DoiVersionArgumentResolver.REVISION_PARAMETER;
        versionNumber = revisionNumber.getAsInt();
      } else {
        versionType = DoiVersionArgumentResolver.INGESTION_PARAMETER;
        versionNumber = articleId.getIngestionNumber();
      }

      // Pre-build a snippet of a URL, meant to be concatenated onto a link in an HTML attribute.
      // Assumes that it will always be preceded by at least one other parameter,
      // else we would need a question mark instead of an ampersand.
      // TODO: Build the URL syntax in XSLT instead
      versionLinkParameter = "&" + versionType + "=" + versionNumber;
    } else {
      versionLinkParameter = "";
    }
    transformer.setParameter("versionLinkParameter", versionLinkParameter);
  }

  private void transform(Site site, InputStream xml, OutputStream html, TransformerInitializer initialization)
      throws IOException {
    Objects.requireNonNull(site);
    Objects.requireNonNull(xml);
    Objects.requireNonNull(html);

    log.debug("Starting XML transformation");
    SAXParserFactory spf = SAXParserFactory.newInstance();
    XMLReader xmlr;
    try {
      SAXParser sp = spf.newSAXParser();
      xmlr = sp.getXMLReader();
    } catch (ParserConfigurationException | SAXException e) {
      throw new RuntimeException(e);
    }

    /*
     * This is a little unorthodox.  Without setting this custom EntityResolver, the transform will
     * make ~50 HTTP calls to nlm.nih.gov to retrieve the DTD and various entity files referenced
     * in the article XML. By setting a custom EntityResolver that just returns an empty string
     * for each of these, we prevent that.  This seems to have no ill effects on the transformation
     * itself.  This is a roundabout way of turning off DTD validation, which is more
     * straightforward to do with a Document/DocumentBuilder, but the saxon library we're using
     * is much faster at XSLT if it uses its own XML parser instead of DocumentBuilder.  See
     * http://stackoverflow.com/questions/155101/make-documentbuilder-parse-ignore-dtd-references
     * for a discussion.
     */
    xmlr.setEntityResolver((String publicId, String systemId) -> {
      // Note: returning null here will cause the HTTP request to be made.

      if (VALID_DTDS.contains(systemId)) {
        return new InputSource(new StringReader(""));
      } else {
        throw new IllegalArgumentException("Unexpected entity encountered: " + systemId);
      }
    });
    // build the transformer and add any context-dependent parameters required for the transform
    // NOTE: the XMLReader is passed here for use in creating any required secondary SAX sources
    Transformer transformer = buildTransformer(site, xmlr, initialization);
    SAXSource saxSource = new SAXSource(xmlr, new InputSource(xml));
    try {
      transformer.transform(saxSource, new StreamResult(html));
    } catch (TransformerException e) {
      throw new RuntimeException(e);
    }

    log.debug("Finished XML transformation");
  }

  /**
   * Enclose an excerpt from article XML in a tag pair, then transform the created element into presentation HTML using
   * the XSL transformation specified for a site.
   *
   * @param site       the site on which the excerpt will be rendered
   * @param xmlExcerpt the XML code to transform
   * @return the presentation HTML
   * @throws TransformerException if an error occurs when applying the transformation
   */
  private String transformExcerpt(Site site, String xmlExcerpt, TransformerInitializer initialization) {
    Objects.requireNonNull(site);
    Objects.requireNonNull(xmlExcerpt);
    StringWriter html = new StringWriter();
    OutputStream outputStream = new WriterOutputStream(html, charset);
    InputStream inputStream = IOUtils.toInputStream(xmlExcerpt, charset);
    try {
      transform(site, inputStream, outputStream, initialization);
      outputStream.close(); // to flush (StringWriter doesn't require a finally block)
    } catch (IOException e) {
      throw new RuntimeException(e); // unexpected, since both streams are in memory
    }
    return html.toString();
  }

  @Override
  public String transformAmendmentBody(Site site, ArticlePointer amendmentId, String xmlExcerpt) {
    return transformExcerpt(site, xmlExcerpt,
        (xmlReader, transformer) -> setVersionLink(amendmentId, transformer));
  }

  @Override
  public String transformImageDescription(Site site, ArticlePointer parentArticleId, String description) {
    String descriptionHtml = transformExcerpt(site, description,
        (xmlReader, transformer) -> setVersionLink(parentArticleId, transformer));
    return kludgeRelativeImageLinks(descriptionHtml);
  }

  /**
   * The transform is written assuming we're at the article path, but because we're also (probably improperly) reusing
   * it here, the paths are wrong. Unlike in FreeMarker, there's no apparent, easy way to configure what the path should
   * be on a per-transformation basis. So kludge in the fix after the fact.
   * <p/>
   * TODO something less horrible
   */
  private static String kludgeRelativeImageLinks(String descriptionHtml) {
    return descriptionHtml.replace("<img src=\"article/", "<img src=\"../article/");
  }

}
