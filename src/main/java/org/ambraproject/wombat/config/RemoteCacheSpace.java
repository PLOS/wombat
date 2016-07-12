package org.ambraproject.wombat.config;

import javax.cache.expiry.Duration;
import java.util.concurrent.TimeUnit;

public enum RemoteCacheSpace {
  ARTICLE_API("articleApi"),
  USER_API("userApi"),
  ARTICLE_HTML("articleHtml"),
  AMENDMENT_BODY("amendmentBody"),
  SITE_CONTENT_METADATA("siteContentMetadata"),
  EXTERNAL_RESOURCE("externalResource"),
  EDITORIAL_CONTENT("editorialContent");

  private final String cacheName;
  private final Duration timeToLive;

  private RemoteCacheSpace(String cacheName) {
    this(cacheName,
        null); // would be DEFAULT_TTL but static vars aren't accessible here; check in getter instead
  }

  private RemoteCacheSpace(String cacheName, Duration timeToLive) {
    this.cacheName = cacheName;
    this.timeToLive = timeToLive;
  }

  private static final Duration DEFAULT_TTL = new Duration(TimeUnit.HOURS, 1);

  /**
   * @return the name to provide to {@link javax.cache.CacheManager} when creating or getting a cache
   */
  String getCacheName() {
    return cacheName;
  }

  Duration getTimeToLive() {
    return (timeToLive == null) ? DEFAULT_TTL : timeToLive;
  }
}
