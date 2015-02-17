package org.ambraproject.wombat.service.remote;

import com.google.common.base.Optional;
import org.ambraproject.wombat.config.RuntimeConfigurationException;
import org.ambraproject.wombat.freemarker.SitePageContext;
import org.ambraproject.wombat.util.CacheParams;
import org.ambraproject.wombat.util.HtmlAttributeTransformation;
import org.ambraproject.wombat.util.HtmlElementSubstitution;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;

/**
 * Retrieves stored blocks of homepage content from a remote service.
 */
public class FetchHtmlServiceImpl implements FetchHtmlService {


  @Autowired
  private EditorialContentService editorialContentService;

  /**
   * {@inheritDoc}
   * <p/>
   * Applies transforms to HTML attributes and performs substitution of placeholder HTML elements with stored content
   */
  @Override
  public Reader readHtml(final SitePageContext sitePageContext, String pageType, String key,
                         final EnumSet<HtmlAttributeTransformation> transformations,
                         final Collection<HtmlElementSubstitution> substitutions)
          throws IOException {
    Map<String, Object> pageConfig = sitePageContext.getSite().getTheme().getConfigMap(pageType);
    String cacheKeyPrefix = (String) pageConfig.get("cacheKeyPrefix");
    if (cacheKeyPrefix == null) {
      throw new RuntimeConfigurationException("No cache key prefix configured for page type: " + pageType);
    }
    String cacheKey = cacheKeyPrefix.concat(":").concat(key);
    Number cacheTtl = (Number) pageConfig.get("cacheTtl");
    CacheParams cacheParams = CacheParams.create(cacheKey, (cacheTtl == null) ? null : cacheTtl.intValue());
    Optional<Integer> version = Optional.absent();     // TODO May want to support page versioning at some point using fetchHtmlDirective

    String transformedHtml = editorialContentService.requestCachedReader(cacheParams, key, version, new CacheDeserializer<Reader, String>() {
      @Override
      public String read(Reader htmlReader) throws IOException {
        // It would be nice to feed the reader directly into the parser, but Jsoup's API makes this awkward.
        // The whole document will be in memory anyway, so buffering it into a string is no great performance loss.
        String htmlString = IOUtils.toString(htmlReader);
        Document document = Jsoup.parseBodyFragment(htmlString);

        for (HtmlAttributeTransformation transformation : transformations) {
          transformation.apply(sitePageContext, document);
        }
        for (HtmlElementSubstitution substitution : substitutions) {
          substitution.substitute(document);
        }

        // We received a snippet, which Jsoup has automatically turned into a complete HTML document.
        // We want to return only the transformed snippet, so retrieve it from the body tag.
        return document.getElementsByTag("body").html();
      }
    });
    return new StringReader(transformedHtml);
  }


}
