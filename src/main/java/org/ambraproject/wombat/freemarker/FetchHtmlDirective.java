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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSetMultimap;
import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.ambraproject.wombat.config.site.SiteResolver;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.remote.EditorialContentApi;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Set;

public class FetchHtmlDirective implements TemplateDirectiveModel {
  private static final Logger log = LoggerFactory.getLogger(FetchHtmlDirective.class);

  @Autowired
  private SiteResolver siteResolver;
  @Autowired
  private EditorialContentApi editorialContentApi;

  private static final String SUBST_ATTR_NAME = "data-subst";

  private static final ImmutableSetMultimap<String, HtmlElementTransformation> ELEMENT_TRANSFORMS =
          ImmutableSetMultimap.<String, HtmlElementTransformation>builder()
                  .putAll("homepage", HtmlElementTransformation.IMAGE,
                                      HtmlElementTransformation.ARTICLE)
                  .putAll("siteContent", HtmlElementTransformation.IMAGE,
                                         HtmlElementTransformation.LINK,
                                         HtmlElementTransformation.ASSET)
                  .build();

  @Override
  public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
      throws TemplateException, IOException {
    Object typeObj = params.get("type");
    if (typeObj == null) {
      throw new TemplateModelException("type parameter required");
    }

    Object pathObj = params.get("path");
    if (pathObj == null) {
      throw new TemplateModelException("path parameter required");
    }

    String pageType = typeObj.toString();

    Set<HtmlElementTransformation> transformations = ELEMENT_TRANSFORMS.get(pageType);
    if (transformations == null) {
      throw new TemplateModelException(String.format("type parameter '%s' is invalid.", pageType));
    }

    ImmutableList<HtmlElementSubstitution> substitutions = HtmlElementSubstitution.buildList(body, SUBST_ATTR_NAME);

    SitePageContext sitePageContext = new SitePageContext(siteResolver, env);

    try (Reader html = editorialContentApi.readHtml(sitePageContext, pageType, pathObj.toString(),
            transformations, substitutions)) {
      IOUtils.copy(html, env.getOut());
    } catch (EntityNotFoundException e) {
      // TODO: Allow themes to provide custom, user-visible error blocks
      log.error("Could not retrieve HTML of type \"{}\" at path \"{}\"", typeObj, pathObj);
    }
  }


}
