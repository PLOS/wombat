package org.ambraproject.wombat.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.io.Closer;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.theme.Theme;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.WriterOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public class ArticleTransformServiceImpl implements ArticleTransformService {
  private static final Logger log = LoggerFactory.getLogger(ArticleTransformServiceImpl.class);

  private static final String TEMPLATE_ROOT_PATH = "xform/";
  private static final String TRANSFORM_TEMPLATE_PATH = TEMPLATE_ROOT_PATH + "article-transform.xsl";

  @Autowired
  private SiteSet siteSet;
  @Autowired
  private RuntimeConfiguration runtimeConfiguration;
  @Autowired
  private Charset charset;

  private static TransformerFactory newTransformerFactory() {
    // This implementation is required for XSLT features, so just hard-code it here
    // Preferred over TransformerFactory.newInstance because Java system properties can burn in hell
    return new net.sf.saxon.TransformerFactoryImpl();
  }


  private static class ThemeUriResolver implements URIResolver, Closeable {

    /*
     * Any stream opened while providing a Source gets stored here,
     * then is closed when the outer ThemeUriResolver object is closed.
     */
    private final Closer closer = Closer.create();

    private final Theme theme;

    private ThemeUriResolver(Theme theme) {
      this.theme = Preconditions.checkNotNull(theme);
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
      InputStream resourceStream;
      try {
        resourceStream = closer.register(theme.getStaticResource(TEMPLATE_ROOT_PATH + href));
      } catch (IOException e) {
        throw new TransformerException(e);
      }
      return new StreamSource(resourceStream);
    }

    @Override
    public void close() throws IOException {
      closer.close();
    }
  }


  private final Map<String, Transformer> cache = Maps.newHashMap();

  /**
   * Access the transformer for a site. Either builds it or retrieves it from a cache.
   *
   * @param site the site key
   * @return the transformer
   * @throws IOException
   */
  private Transformer getTransformer(Site site) throws IOException {
    /*
     * Use a simple, hashtable-based cache. This assumes that the number of sites (and the size of the transfomers)
     * will never be so large that evicting a cached transformer makes sense.
     *
     * This prevents the application from picking up any dynamic changes to the transform in a theme (such as an *.xsl
     * file on disk being overwritten at runtime).
     */
    String siteKey = site.getKey();
    Transformer transformer = cache.get(siteKey);
    if (transformer == null) {
      transformer = buildTransformer(site);
      cache.put(siteKey, transformer);
    }
    return transformer;
  }

  /**
   * Build a new transformer for a site.
   *
   * @param site the site key
   * @return the transformer
   * @throws IOException
   */
  private Transformer buildTransformer(Site site) throws IOException {
    Theme theme = site.getTheme();
    log.info("Building transformer for: {}", site);
    TransformerFactory factory = newTransformerFactory();

    try (ThemeUriResolver resolver = new ThemeUriResolver(theme);
         InputStream transformFile = theme.getStaticResource(TRANSFORM_TEMPLATE_PATH)) {
      factory.setURIResolver(resolver);
      return factory.newTransformer(new StreamSource(transformFile));
    } catch (TransformerConfigurationException e) {
      throw new RuntimeException(e);
    }
  }


  @Override
  public void transform(Site site, InputStream xml, OutputStream html)
      throws IOException, TransformerException {
    Preconditions.checkNotNull(site);
    Preconditions.checkNotNull(xml);
    Preconditions.checkNotNull(html);

    Transformer transformer = getTransformer(site);
    log.debug("Starting XML transformation");
    SAXParserFactory spf = SAXParserFactory.newInstance();
    XMLReader xmlr;
    try {
      SAXParser sp = spf.newSAXParser();
      xmlr = sp.getXMLReader();
    } catch (ParserConfigurationException | SAXException e) {
      throw new TransformerException(e);
    }

    // This is a little unorthodox.  Without setting this custom EntityResolver, the transform will
    // make ~50 HTTP calls to nlm.nih.gov to retrieve the DTD and various entity files referenced
    // in the article XML.  By setting a custom EntityResolver that just returns an empty string
    // for each of these, we prevent that.  This seems to have no ill effects on the transformation
    // itself.  This is a roundabout way of turning off DTD validation, which is more
    // straightforward to do with a Document/DocumentBuilder, but the saxon library we're using
    // is much faster at XSLT if it uses its own XML parser instead of DocumentBuilder.  See
    // http://stackoverflow.com/questions/155101/make-documentbuilder-parse-ignore-dtd-references
    // for a discussion.
    xmlr.setEntityResolver(new EntityResolver() {
      @Override
      public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {

        // Note: returning null here will cause the HTTP request to be made.

        if ("http://dtd.nlm.nih.gov/publishing/3.0/journalpublishing3.dtd".equals(systemId)) {
          return new InputSource(new StringReader(""));
        } else {
          throw new IllegalArgumentException("Unexpected entity encountered: " + systemId);
        }
      }
    });
    SAXSource saxSource = new SAXSource(xmlr, new InputSource(xml));
    transformer.transform(saxSource, new StreamResult(html));

    log.debug("Finished XML transformation");
  }

  @Override
  public void transformExcerpt(Site site, InputStream xmlExcerpt, OutputStream html, String enclosingTag)
      throws IOException, TransformerException {
    Preconditions.checkNotNull(site);
    Preconditions.checkNotNull(xmlExcerpt);
    Preconditions.checkNotNull(html);

    InputStream streamToTransform;
    if (Strings.isNullOrEmpty(enclosingTag)) {
      streamToTransform = xmlExcerpt;
    } else {
      // Build a stream that contains the original stream surrounded by tags.
      // To avoid dumping the original stream into memory, append two mini-streams.
      InputStream prefix = IOUtils.toInputStream('<' + enclosingTag + '>', charset);
      InputStream suffix = IOUtils.toInputStream("</" + enclosingTag + '>', charset);
      List<InputStream> concatenation = ImmutableList.of(prefix, xmlExcerpt, suffix);
      streamToTransform = new SequenceInputStream(Iterators.asEnumeration(concatenation.iterator()));
    }

    transform(site, streamToTransform, html);
  }

  @Override
  public String transformExcerpt(Site site, String xmlExcerpt, String enclosingTag) throws TransformerException {
    StringWriter html = new StringWriter();
    OutputStream outputStream = new WriterOutputStream(html, charset);
    InputStream inputStream = IOUtils.toInputStream(xmlExcerpt, charset);
    try {
      transformExcerpt(site, inputStream, outputStream, enclosingTag);
      outputStream.close(); // to flush (StringWriter doesn't require a finally block)
    } catch (IOException e) {
      throw new RuntimeException(e); // unexpected, since both streams are in memory
    }
    return html.toString();
  }

}
