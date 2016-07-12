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
  EDITORIAL_CONTENT("editorialContent", new Duration(TimeUnit.MINUTES, 30));

  private final String cacheName;
  private final Duration timeToLive;

  private RemoteCacheSpace(String cacheName) {
    this(cacheName, CacheManagerWrapper.DEFAULT_TTL);
  }

  private RemoteCacheSpace(String cacheName, Duration timeToLive) {
    this.cacheName = cacheName;
    this.timeToLive = timeToLive;
  }

  /**
   * @return the name to provide to {@link javax.cache.CacheManager} when creating or getting a cache
   */
  String getCacheName() {
    return cacheName;
  }

  Duration getTimeToLive() {
    return timeToLive;
  }
}
