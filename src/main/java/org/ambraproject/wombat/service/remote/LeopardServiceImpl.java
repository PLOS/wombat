package org.ambraproject.wombat.service.remote;

import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.util.UriUtil;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;

public class LeopardServiceImpl implements LeopardService {

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;
  @Autowired
  private CachedRemoteService<Reader> cachedRemoteReader;

  @Override
  public String readHtml(String path) throws IOException {
    // TODO: Cache
    URI address = UriUtil.concatenate(runtimeConfiguration.getLeopardServer(), path);

    Document document;
    try (Reader htmlFromLeopardService = cachedRemoteReader.request(address)) {
      // It would be nice to feed the reader directly into the parser, but Jsoup's API makes this awkward.
      // The whole document will be in memory anyway, so buffering it into a string is no great performance loss.
      String htmlString = IOUtils.toString(htmlFromLeopardService);
      document = Jsoup.parse(htmlString);
    }

    for (AttributeTransformation transformation : AttributeTransformation.values()) {
      transformation.apply(document);
    }

    // We received a snippet, which Jsoup has automatically turned into a complete HTML document.
    // We want to return only the transformed snippet, so retrieve it from the body tag.
    return document.getElementsByTag("body").html();
  }

  private static enum AttributeTransformation {
    IMAGE("img", "data-hash", "src") {
      @Override
      protected String transformAttribute(String value) {
        return "indirect/" + value; // Placeholder. TODO: Actual implementation
      }
    },

    ARTICLE("a", "data-doi", "href") {
      @Override
      protected String transformAttribute(String value) {
        return "article?id=" + value; // Placeholder. TODO: Actual implementation
      }
    };

    private final String tagName; // Search tags with this name
    private final String sourceAttributeKey; // Find attributes with this key
    private final String targetAttributeKey; // Replace source attribute with this key, with transformed value

    /**
     * Transform the source attribute value into the target attribute value.
     *
     * @param value the source attribute value
     * @return the target attribute value
     */
    protected abstract String transformAttribute(String value);

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
    public void apply(Document document) {
      for (Element element : document.getElementsByAttribute(sourceAttributeKey)) {
        if (element.tagName().equals(tagName)) {
          String oldValue = element.attr(sourceAttributeKey);
          String newValue = transformAttribute(oldValue);

          element.removeAttr(sourceAttributeKey);
          element.attr(targetAttributeKey, newValue);
        }
      }
    }
  }

}
