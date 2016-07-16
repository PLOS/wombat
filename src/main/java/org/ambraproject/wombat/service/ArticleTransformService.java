package org.ambraproject.wombat.service;

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
   * @param renderContext provides the context for rendering the article (wraps the site and optional context data)
   * @param xml  a stream containing article XML
   * @param html the stream that will receive the presentation HTML
   * @param references list of parsed references
   * @throws IOException          if either stream cannot be read
   * @throws TransformerException if an error occurs when applying the transformation
   */
  public abstract void transform(RenderContext renderContext, InputStream xml, OutputStream html,
      List<Reference> references) throws IOException, TransformerException;


  /**
   * Enclose an excerpt from article XML in a tag pair, then transform the created element into presentation HTML using
   * the XSL transformation specified for a site.
   *
   * @param renderContext provides the context for rendering the excerpt (wraps the site and optional context data)
   * @param xmlExcerpt   a stream containing the XML code to transform
   * @param html         the stream that will receive the presentation HTML
   * @param enclosingTag the XML tag (without angle brackets) to put before and after the XML to be transformed, or
   *                     {@code null} to use none
   * @throws IOException          if either stream cannot be read
   * @throws TransformerException if an error occurs when applying the transformation
   */
  public abstract void transformExcerpt(RenderContext renderContext, InputStream xmlExcerpt, OutputStream html, String enclosingTag)
      throws IOException, TransformerException;

  /**
   * Enclose an excerpt from article XML in a tag pair, then transform the created element into presentation HTML using
   * the XSL transformation specified for a site.
   *
   * @param renderContext provides the context for rendering the excerpt (wraps the site and optional context data)
   * @param xmlExcerpt   the XML code to transform
   * @param enclosingTag the XML tag (without angle brackets) to put before and after the XML to be transformed, or
   *                     {@code null} to use none
   * @return the presentation HTML
   * @throws IOException          if either stream cannot be read
   * @throws TransformerException if an error occurs when applying the transformation
   */
  public abstract String transformExcerpt(RenderContext renderContext, String xmlExcerpt, String enclosingTag)
      throws TransformerException;

  public String transformImageDescription(RenderContext renderContext, String description);
}
