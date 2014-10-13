package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.site.Site;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ArticleTransformService {

  /**
   * Transform an article's XML document into presentation HTML, using the XSL transformation specified for a site.
   *
   * @param site the site whose XSL transformation is to be used
   * @param xml  a stream containing article XML
   * @param html the stream that will receive the presentation HTML
   * @throws IOException          if either stream cannot be read
   * @throws TransformerException if an error occurs when applying the transformation
   */
  public abstract void transform(Site site, InputStream xml, OutputStream html)
      throws IOException, TransformerException;

  /**
   * Enclose an excerpt from article XML in a tag pair, then transform the created element into presentation HTML using
   * the XSL transformation specified for a site.
   *
   * @param site         the site whose XSL transformation is to be used
   * @param xmlExcerpt   a stream containing the XML code to transform
   * @param html         the stream that will receive the presentation HTML
   * @param enclosingTag the XML tag (without angle brackets) to put before and after the XML to be transformed, or
   *                     {@code null} to use none
   * @throws IOException          if either stream cannot be read
   * @throws TransformerException if an error occurs when applying the transformation
   */
  public abstract void transformExcerpt(Site site, InputStream xmlExcerpt, OutputStream html, String enclosingTag)
      throws IOException, TransformerException;

  /**
   * Enclose an excerpt from article XML in a tag pair, then transform the created element into presentation HTML using
   * the XSL transformation specified for a site.
   *
   * @param site         the site whose XSL transformation is to be used
   * @param xmlExcerpt   the XML code to transform
   * @param enclosingTag the XML tag (without angle brackets) to put before and after the XML to be transformed, or
   *                     {@code null} to use none
   * @return the presentation HTML
   * @throws IOException          if either stream cannot be read
   * @throws TransformerException if an error occurs when applying the transformation
   */
  public abstract String transformExcerpt(Site site, String xmlExcerpt, String enclosingTag)
      throws TransformerException;

}
