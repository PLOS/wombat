package org.ambraproject.wombat.service;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ArticleTransformService {

  /**
   * Transform an article's XML document into presentation HTML, using the XSL transformation specified for a site.
   *
   * @param siteKey the key of the site whose XSL transformation is to be used
   * @param xml     a stream containing article XML
   * @param html    the stream that will receive the presentation HTML
   * @throws IOException          if either stream cannot be read
   * @throws TransformerException if an error occurs when applying the transformation
   */
  public abstract void transform(String siteKey, InputStream xml, OutputStream html)
      throws IOException, TransformerException;

  /**
   * Enclose an excerpt from article XML in a tag pair, then transform the created element into presentation HTML using
   * the XSL transformation specified for a site.
   *
   * @param siteKey      the key of the site whose XSL transformation is to be used
   * @param xmlExcerpt   a stream containing the XML code to transform
   * @param html         the stream that will receive the presentation HTML
   * @param enclosingTag the XML tag (without angle brackets) to put before and after the XML to be transformed
   * @throws IOException          if either stream cannot be read
   * @throws TransformerException if an error occurs when applying the transformation
   */
  public abstract void transformExcerpt(String siteKey, InputStream xmlExcerpt, OutputStream html, String enclosingTag)
      throws IOException, TransformerException;

  /**
   * Enclose an excerpt from article XML in a tag pair, then transform the created element into presentation HTML using
   * the XSL transformation specified for a site.
   *
   * @param siteKey      the key of the site whose XSL transformation is to be used
   * @param xmlExcerpt   the XML code to transform
   * @param enclosingTag the XML tag (without angle brackets) to put before and after the XML to be transformed
   * @return the presentation HTML
   * @throws IOException          if either stream cannot be read
   * @throws TransformerException if an error occurs when applying the transformation
   */
  public abstract String transformExcerpt(String siteKey, String xmlExcerpt, String enclosingTag)
      throws TransformerException;

}
