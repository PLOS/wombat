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

import org.ambraproject.wombat.config.site.SiteSet;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public enum HtmlElementTransformation {
  IMAGE("img", "data-lemur-key") {
    @Override
    protected void transformElement(Element element, SitePageContext sitePageContext, SiteSet siteSet) {
      String newValue = sitePageContext.buildLink("indirect/" + getAttrValueAndRemove(element));
      element.attr("src", newValue);
    }
  },

  ARTICLE("a", "data-lemur-doi") {
    @Override
    protected void transformElement(Element element, SitePageContext sitePageContext, SiteSet siteSet) {
      String newValue = sitePageContext.buildLink("article?id=" + getAttrValueAndRemove(element));
      element.attr("href", newValue);
    }
  },

  ASSET("a", "data-lemur-key") {
    @Override
    protected void transformElement(Element element, SitePageContext sitePageContext, SiteSet siteSet) {
      String newValue = sitePageContext.buildLink("s/file?id=" + getAttrValueAndRemove(element));
      element.attr("href", newValue);
    }
  },

  LINK("a", "data-lemur-link") {
    @Override
    protected void transformElement(Element element, SitePageContext sitePageContext, SiteSet siteSet) {
      String path = getAttrValueAndRemove(element);
      String journalKey = getAttrValueAndRemove(element, "data-lemur-link-journal");
      String suffix = getAttrValueAndRemove(element, "data-lemur-link-suffix");
      if (journalKey.isEmpty()) {
        path = sitePageContext.buildLink(path);
      } else {
        // cross-journal link
        path = sitePageContext.buildLink(siteSet, journalKey, path);
      }
      element.attr("href", path + suffix);
    }
  };

  private final String matchingAttributeName; // Find elements with this attribute
  private final String matchingTagName; // Filter elements by tag name

  /**
   * Transform the element containing the matching attribute and tag name
   *
   * @param element the HTML element to be transformed
   * @param sitePageContext the context of the HTML page
   * @param siteSet encapsulates all hosted sites and their request scheme used to resolve to and from urls,
   *                provided here for use in transformations which require cross-site references
   */
  protected abstract void transformElement(Element element, SitePageContext sitePageContext, SiteSet siteSet);

  /**
   * Retrieve the value for the given attribute and remove from the element. This is a useful way to ensure
   * that any transform-specific attribute values consumed for the creation of new values are cleaned up.
   *
   * @param element the HTML element to be transformed
   * @param attrKey the name for the attribute
   * @return the attribute value, or empty string if attribute is not present
   */
  protected String getAttrValueAndRemove(Element element, String attrKey) {
    if (element.hasAttr(attrKey)){
      String attrVal = element.attr(attrKey);
      element.removeAttr(attrKey);
      return attrVal;
    }
    return "";
  }

  /**
   * Retrieve the value for the matching attribute (the attribute used to find the given element), and remove
   * from the element. This is a useful way to ensure that any transform-specific attribute values consumed for
   * the creation of new values are cleaned up.
   *
   * @param element the HTML element to be transformed
   * @return the target attribute value
   */
  protected String getAttrValueAndRemove(Element element) {
    return getAttrValueAndRemove(element, this.matchingAttributeName);
  }

  private HtmlElementTransformation(String tagName, String attributeName) {
    this.matchingTagName = tagName;
    this.matchingAttributeName = attributeName;
  }

  /**
   * Apply a transformation for each HTML element with an attribute and tag name as described in this object
   *
   * @param sitePageContext the context of the HTML page
   * @param siteSet encapsulates all hosted sites and their request scheme used to resolve to and from urls,
   *                provided here for use in transformations which require cross-site references
   * @param document the document to modify in place
   */
  public void apply(SitePageContext sitePageContext, SiteSet siteSet, Document document) {
    for (Element element : document.getElementsByAttribute(matchingAttributeName)) {
      if (element.tagName().equals(matchingTagName)) {
        transformElement(element, sitePageContext, siteSet);
      }
    }
  }
}