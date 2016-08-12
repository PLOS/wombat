package org.ambraproject.wombat.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteResolver;
import org.ambraproject.wombat.service.SiteTransformerFactory;
import org.ambraproject.wombat.service.XmlUtil;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ArticleExcerptTransformDirective extends VariableLookupDirective<String> {

  @Autowired
  private SiteResolver siteResolver;

  private static final SiteTransformerFactory SITE_TRANSFORMER_FACTORY = new SiteTransformerFactory(
      "xform/", "light.xsl");


  /**
   * Cache of built {@link Transformer} objects.
   * <p>
   * Although the {@code Transformer} class is documented as being non-thread-safe, we expect there to be no harmful
   * race conditions because we never call any methods that change the state of the {@code Transformer} (such as {@link
   * Transformer#setParameter}); we only call {@link Transformer#transform}. (Note that {@link #getTransformer} might
   * occasionally create two {@code Transformer} objects for the same {@code Site} concurrently, but this is harmless.)
   * <p>
   * This cache has no expiration because the values never change (assuming that theme content isn't swapped) and the
   * number of entries can't ever exceed the number of {@link Site} objects.
   */
  private final Map<Site, Transformer> transformerCache = Collections.synchronizedMap(new HashMap<>());

  private Transformer getTransformer(Site site) throws IOException {
    Transformer transformer = transformerCache.get(site);
    if (transformer == null) {
      transformer = SITE_TRANSFORMER_FACTORY.build(site);
      transformerCache.put(site, transformer);
    }
    return transformer;
  }


  @Override
  protected String getValue(Environment env, Map params) throws TemplateModelException, IOException {

    Object xmlParam = params.get("xml");
    if (!(xmlParam instanceof TemplateScalarModel)) {
      throw new TemplateModelException("xml param must be a non-null string");
    }
    String xml = ((TemplateScalarModel) xmlParam).getAsString();

    boolean isTextOnly = TemplateModelUtil.getBooleanValue((TemplateModel) params.get("textOnly"));
    if (isTextOnly) {
      return XmlUtil.extractText(xml);
    }

    Site site = new SitePageContext(siteResolver, env).getSite();
    Transformer transformer = getTransformer(site);
    StringWriter html = new StringWriter();
    try {
      transformer.transform(new StreamSource(new StringReader(xml)), new StreamResult(html));
    } catch (TransformerException e) {
      throw new RuntimeException(e);
    }
    return html.toString();
  }

}
