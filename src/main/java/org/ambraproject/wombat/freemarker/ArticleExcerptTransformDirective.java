package org.ambraproject.wombat.freemarker;

import freemarker.core.Environment;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteResolver;
import org.ambraproject.wombat.service.SiteTransformerFactory;
import org.ambraproject.wombat.service.XmlUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

public class ArticleExcerptTransformDirective extends VariableLookupDirective<String> {

  @Autowired
  private SiteResolver siteResolver;

  private static final SiteTransformerFactory SITE_TRANSFORMER_FACTORY = new SiteTransformerFactory(
      "xform/", "light.xsl");

  @Override
  protected String getValue(Environment env, Map params) throws TemplateModelException, IOException {

    Object xmlParam = params.get("xml");
    if (!(xmlParam instanceof TemplateScalarModel)) {
      throw new TemplateModelException("xml param must be a non-null string");
    }
    String xml = ((TemplateScalarModel) xmlParam).getAsString();

    boolean isTextOnly = TemplateModelUtil.getBooleanValue((TemplateModel) params.get("textOnly"));
    if (isTextOnly) {
      return StringEscapeUtils.escapeHtml(XmlUtil.extractText(xml));
    }

    Site site = new SitePageContext(siteResolver, env).getSite();
    Transformer transformer = SITE_TRANSFORMER_FACTORY.build(site);
    StringWriter html = new StringWriter();
    try {
      transformer.transform(new StreamSource(new StringReader(xml)), new StreamResult(html));
    } catch (TransformerException e) {
      throw new RuntimeException(e);
    }
    return html.toString();
  }

}
