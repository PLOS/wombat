/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

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
