package org.ambraproject.wombat.service.remote;

import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.freemarker.HtmlElementSubstitution;
import org.ambraproject.wombat.freemarker.HtmlElementTransformation;
import org.ambraproject.wombat.freemarker.SitePageContext;
import org.ambraproject.wombat.util.CacheKey;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class EditorialContentApiImpl extends AbstractContentApi implements EditorialContentApi {

  @Autowired
  private SiteSet siteSet;

  @Override
  protected String getRepoConfigKey() {
    return "editorial";
  }

  /**
   * {@inheritDoc}
   * <p/>
   * Applies transforms to HTML attributes and performs substitution of placeholder HTML elements with stored content
   */
  @Override
  public Reader readHtml(final SitePageContext sitePageContext, String pageType, String key,
                         final Set<HtmlElementTransformation> transformations,
                         final Collection<HtmlElementSubstitution> substitutions)
          throws IOException {
    Map<String, Object> pageConfig = sitePageContext.getSite().getTheme().getConfigMap(pageType);
    ContentKey version = ContentKey.createForLatestVersion(key); // TODO May want to support page versioning at some point using fetchHtmlDirective
    CacheKey cacheKey = CacheKey.create(pageType, key);
    Number cacheTtl = (Number) pageConfig.get("cacheTtl");
    if (cacheTtl != null) {
      cacheKey = cacheKey.addTimeToLive(cacheTtl.intValue());
    }

    String transformedHtml = requestCachedReader(cacheKey, version, new CacheDeserializer<Reader, String>() {
      @Override
      public String read(Reader htmlReader) throws IOException {
        // It would be nice to feed the reader directly into the parser, but Jsoup's API makes this awkward.
        // The whole document will be in memory anyway, so buffering it into a string is no great performance loss.
        String htmlString = IOUtils.toString(htmlReader);
        Document document = Jsoup.parseBodyFragment(htmlString);

        for (HtmlElementTransformation transformation : transformations) {
          transformation.apply(sitePageContext, siteSet, document);
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

  /**
   * {@inheritDoc}
   * <p/>
   * Returns a JSON object from a remote service
   */
  @Override
  public Object getJson(String pageType, String key) throws IOException {
    CacheKey cacheKey = CacheKey.create(pageType, key);
    ContentKey version = ContentKey.createForLatestVersion(key);
    return requestCachedReader(cacheKey, version, jsonReader -> gson.fromJson(jsonReader, Object.class));
  }
}
