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

import org.ambraproject.wombat.freemarker.HtmlElementSubstitution;
import org.ambraproject.wombat.freemarker.HtmlElementTransformation;
import org.ambraproject.wombat.freemarker.SitePageContext;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Set;

public interface EditorialContentApi extends ContentApi {

  /**
   * Fetch a block of HTML from
   * Transform the raw HTML receives from a remote service.
   *
   * @param sitePageContext the site of the context into which the HTML will be inserted
   * @param key             a key identifying the HTML to fetch
   * @param substitutions   substitutions to apply to the HTML
   * @param transformations transformations to apply to the HTML elements
   * @return an HTML block
   * @throws IOException
   */
  public Reader readHtml(SitePageContext sitePageContext, String pageType, String key,
                         Set<HtmlElementTransformation> transformations,
                         Collection<HtmlElementSubstitution> substitutions) throws IOException;

  /**
   * Fetch a JSON object from a remote service.
   *
   * @param key             a key identifying the JSON string to fetch
   * @return an HTML block
   * @throws IOException
   */
  public Object getJson(String pageType, String key) throws IOException;

}
