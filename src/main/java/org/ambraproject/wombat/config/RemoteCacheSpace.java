package org.ambraproject.wombat.config;

import org.ambraproject.wombat.service.remote.RemoteCacheKey;

import javax.cache.expiry.Duration;
import java.util.concurrent.TimeUnit;

public enum RemoteCacheSpace {
  ARTICLE_API("articleApi", RemoteCacheKey.class, 10_000, 1, TimeUnit.HOURS),
  USER_API("userApi", RemoteCacheKey.class, 10_000, 1, TimeUnit.HOURS),
  ARTICLE_HTML("articleHtml", RemoteCacheKey.class, 10_000, 1, TimeUnit.HOURS),
  AMENDMENT_BODY("amendmentBody", RemoteCacheKey.class, 10_000, 1, TimeUnit.HOURS),
  SITE_CONTENT_METADATA("siteContentMetadata", RemoteCacheKey.class, 10_000, 1, TimeUnit.HOURS),
  EXTERNAL_RESOURCE("externalResource", RemoteCacheKey.class, 10_000, 1, TimeUnit.HOURS),
  EDITORIAL_CONTENT("editorialContent", RemoteCacheKey.class, 10_000, 30, TimeUnit.MINUTES);

  private final String cacheName;
  private final Class<?> clazz;
  private final long entries;
  private final long timeToLive;
  private final TimeUnit timeUnit;

  RemoteCacheSpace(String cacheName, Class<?> clazz, long entries, long timeToLive, TimeUnit timeUnit) {
    this.cacheName = cacheName;
    this.clazz = clazz;
    this.entries = entries;
    this.timeToLive = timeToLive;
    this.timeUnit = timeUnit;
  }

  /**
   * @return the name to provide to {@link javax.cache.CacheManager} when creating or getting a cache
   */
  String getCacheName() {
    return cacheName;
  }
  Class<?> getKeyClass() { return this.clazz; }
  long getEntries() { return timeToLive; }
  long getTimeToLive() { return timeToLive; }
  TimeUnit getTTLUnits() { return timeUnit; }
}
