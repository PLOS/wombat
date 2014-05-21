package org.ambraproject.wombat.service.remote;

import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.freemarker.SitePageContext;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Retrieves stored blocks of homepage content from a remote service.
 */
public class StoredHomepageService implements FetchHtmlService {

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;
  @Autowired
  private SoaService soaService;

  /**
   * {@inheritDoc}
   * <p/>
   * Applies transforms that are particular to the "Lemur" API for homepages.
   */
  @Override
  public Reader readHtml(final SitePageContext sitePageContext, String key) throws IOException {
    String cacheKey = "homepage:" + key;
    String address = String.format("repo/homepages/%s/latest", key);

    String transformedHtml = soaService.requestCachedReader(cacheKey, address, new CacheDeserializer<Reader, String>() {
      @Override
      public String read(Reader htmlReader) throws IOException {
        // It would be nice to feed the reader directly into the parser, but Jsoup's API makes this awkward.
        // The whole document will be in memory anyway, so buffering it into a string is no great performance loss.
        String htmlString = IOUtils.toString(htmlReader);
        Document document = Jsoup.parse(htmlString);

        for (AttributeTransformation transformation : AttributeTransformation.values()) {
          transformation.apply(sitePageContext, document);
        }

        // We received a snippet, which Jsoup has automatically turned into a complete HTML document.
        // We want to return only the transformed snippet, so retrieve it from the body tag.
        return document.getElementsByTag("body").html();
      }
    });
    return new StringReader(transformedHtml);
  }

  private static enum AttributeTransformation {
    IMAGE("img", "data-lemur-key", "src") {
      @Override
      protected String transformAttribute(SitePageContext sitePageContext, String value) {
        String path = String.format("indirect/homepages/%s/latest", value);
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

    private AttributeTransformation(String tagName, String sourceAttributeKey, String targetAttributeKey) {
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

}
