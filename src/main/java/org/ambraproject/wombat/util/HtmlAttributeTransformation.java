package org.ambraproject.wombat.util;

import org.ambraproject.wombat.freemarker.SitePageContext;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public enum HtmlAttributeTransformation {
  IMAGE("img", "data-lemur-key", "src") {
    @Override
    protected String transformAttribute(SitePageContext sitePageContext, String value) {
      String path = "indirect/" + value;
      return sitePageContext.buildLink(path);
    }
  },

  ARTICLE("a", "data-lemur-doi", "href") {
    @Override
    protected String transformAttribute(SitePageContext sitePageContext, String value) {
      return sitePageContext.buildLink("article?id=" + value);
    }
  };

  private final String tagName; // Search tags with this name
  private final String sourceAttributeKey; // Find attributes with this key
  private final String targetAttributeKey; // Replace source attribute with this key, with transformed value

  /**
   * Transform the source attribute value into the target attribute value.
   *
   * @param sitePageContext the context of the HTML page
   * @param value           the source attribute value
   * @return the target attribute value
   */
  protected abstract String transformAttribute(SitePageContext sitePageContext, String value);

  private HtmlAttributeTransformation(String tagName, String sourceAttributeKey, String targetAttributeKey) {
    this.tagName = tagName;
    this.sourceAttributeKey = sourceAttributeKey;
    this.targetAttributeKey = targetAttributeKey;
  }

  /**
   * For each tag with an attribute as described this object: delete the old attribute, apply this object's
   * transformation to the attribute value, and write the target attribute with the transformed value.
   *
   * @param document the document to modify in place
   */
  public void apply(SitePageContext sitePageContext, Document document) {
    for (Element element : document.getElementsByAttribute(sourceAttributeKey)) {
      if (element.tagName().equals(tagName)) {
        String oldValue = element.attr(sourceAttributeKey);
        String newValue = transformAttribute(sitePageContext, oldValue);

        element.removeAttr(sourceAttributeKey);
        element.attr(targetAttributeKey, newValue);
      }
    }
  }
}