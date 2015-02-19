package org.ambraproject.wombat.freemarker;

import freemarker.template.TemplateModelException;
import java.io.IOException;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.freemarker.SitePageContext;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


public enum HtmlAttributeTransformation {
  IMAGE("img", "data-lemur-key", "src") {
    @Override
    protected String transformAttribute(SitePageContext sitePageContext, SiteSet siteSet, String value) {
      String path = "indirect/" + value;
      return sitePageContext.buildLink(path);
    }
  },

  ARTICLE("a", "data-lemur-doi", "href") {
    @Override
    protected String transformAttribute(SitePageContext sitePageContext, SiteSet siteSet, String value) {
      return sitePageContext.buildLink("article?id=" + value);
    }
  },

  ASSET("a", "data-lemur-key", "href") {
    @Override
    protected String transformAttribute(SitePageContext sitePageContext, SiteSet siteSet, String value) {
      String path = "indirect/" + value;
      return sitePageContext.buildLink(path);
    }
  },

  LINK("a", "data-lemur-link", "href") {
    @Override
    protected String transformAttribute(SitePageContext sitePageContext, SiteSet siteSet, String value) {
      int sepIdx = value.indexOf("|");
      if (sepIdx >= 0) {
        // cross-journal link, so extract journal key
        String path = value.substring(sepIdx + 1);
        String journalKey = value.substring(0, sepIdx);
        return sitePageContext.buildLink(siteSet, journalKey, path);
      }
      return sitePageContext.buildLink(value);
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
  protected abstract String transformAttribute(SitePageContext sitePageContext, SiteSet siteSet, String value);

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
  public void apply(SitePageContext sitePageContext, SiteSet siteSet, Document document) {
    for (Element element : document.getElementsByAttribute(sourceAttributeKey)) {
      if (element.tagName().equals(tagName)) {
        String oldValue = element.attr(sourceAttributeKey);
        String newValue = transformAttribute(sitePageContext, siteSet, oldValue);

        element.removeAttr(sourceAttributeKey);
        element.attr(targetAttributeKey, newValue);
      }
    }
  }
}