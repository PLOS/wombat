package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.identity.ArticlePointer;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ArticleTransformService {

  /**
   * Transform an article's XML document into presentation HTML, using the XSL transformation and any additional source
   * data defined for the given context
   *
   * @param site      the context in which to render the article
   * @param articleId the identity of the article to render
   * @param xml       a stream containing article XML
   * @param html      the stream that will receive the presentation HTML
   * @throws IOException          if either stream cannot be read
   * @throws TransformerException if an error occurs when applying the transformation
   */
  public abstract void transformArticle(Site site, ArticlePointer articleId, InputStream xml, OutputStream html)
      throws IOException;

  public abstract String transformAmendmentBody(Site site, ArticlePointer amendmentId, String xmlExcerpt);

  public abstract String transformImageDescription(Site site, ArticlePointer parentArticleId, String description);
}
