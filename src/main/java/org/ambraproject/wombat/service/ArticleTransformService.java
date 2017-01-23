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

package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.model.Reference;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface ArticleTransformService {

  /**
   * Transform an article's XML document into presentation HTML, using the XSL transformation and any additional source
   * data defined for the given context
   *
   * @param site       the context in which to render the article
   * @param articleId  the identity of the article to render
   * @param references list of parsed references
   * @param xml        a stream containing article XML
   * @param html       the stream that will receive the presentation HTML
   * @throws IOException          if either stream cannot be read
   * @throws TransformerException if an error occurs when applying the transformation
   */
  public abstract void transformArticle(Site site, ArticlePointer articleId, List<Reference> references,
                                        InputStream xml, OutputStream html)
      throws IOException;

  public abstract String transformAmendmentBody(Site site, ArticlePointer amendmentId, String xmlExcerpt);

  public abstract String transformImageDescription(Site site, ArticlePointer parentArticleId, String description);
}
