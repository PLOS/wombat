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

package org.ambraproject.wombat.service.remote;

import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.freemarker.HtmlElementSubstitution;
import org.ambraproject.wombat.freemarker.HtmlElementTransformation;
import org.ambraproject.wombat.freemarker.SitePageContext;
import org.ambraproject.wombat.util.CacheKey;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class EditorialContentApiImpl extends AbstractContentApi implements EditorialContentApi {

  @Autowired
  private SiteSet siteSet;

  @Override
  protected String getRepoConfigKey() {
    return "editorial";
  }

  /**
   * {@inheritDoc}
   * <p/>
   * Applies transforms to HTML attributes and performs substitution of placeholder HTML elements with stored content
   */
  @Override
  public Reader readHtml(final SitePageContext sitePageContext, String pageType, String key,
                         final Set<HtmlElementTransformation> transformations,
                         final Collection<HtmlElementSubstitution> substitutions)
          throws IOException {
    Map<String, Object> pageConfig = sitePageContext.getSite().getTheme().getConfigMap(pageType);
    ContentKey version = ContentKey.createForLatestVersion(key); // TODO May want to support page versioning at some point using fetchHtmlDirective
    CacheKey cacheKey = CacheKey.create(pageType, key);
    Number cacheTtl = (Number) pageConfig.get("cacheTtl");
    if (cacheTtl != null) {
      cacheKey = cacheKey.addTimeToLive(cacheTtl.intValue());
    }

    Reader htmlReader = requestReader(version);
    // It would be nice to feed the reader directly into the parser, but Jsoup's API makes this awkward.
    // The whole document will be in memory anyway, so buffering it into a string is no great performance loss.
    String htmlString = IOUtils.toString(htmlReader);
    Document document = Jsoup.parseBodyFragment(htmlString);
    
    for (HtmlElementTransformation transformation : transformations) {
      transformation.apply(sitePageContext, siteSet, document);
    }
    for (HtmlElementSubstitution substitution : substitutions) {
      substitution.substitute(document);
    }
    
    // We received a snippet, which Jsoup has automatically turned into a complete HTML document.
    // We want to return only the transformed snippet, so retrieve it from the body tag.
    String transformedHtml =  document.getElementsByTag("body").html();
    return new StringReader(transformedHtml);
  }

  /**
   * {@inheritDoc}
   * <p/>
   * Returns a JSON object from a remote service
   */
  @Override
  public Object getJson(String pageType, String key) throws IOException {
    ContentKey version = ContentKey.createForLatestVersion(key);
    return gson.fromJson(requestReader(version), Object.class);
  }
}
