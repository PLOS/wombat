package org.ambraproject.wombat.config;

import javax.cache.expiry.Duration;
import java.util.concurrent.TimeUnit;

public enum RemoteCacheSpace {
  ARTICLE_API(),
  USER_API(),
  ARTICLE_HTML(),
  AMENDMENT_BODY(),
  SITE_CONTENT_METADATA(),
  EXTERNAL_RESOURCE(),
  EDITORIAL_CONTENT();

  private final Duration timeToLive;

  private RemoteCacheSpace() {
    this(null); // would be DEFAULT_TTL but static vars aren't accessible here; check in getter instead
  }

  private RemoteCacheSpace(Duration timeToLive) {
    this.timeToLive = timeToLive;
  }

  private static final Duration DEFAULT_TTL = new Duration(TimeUnit.HOURS, 1);

  /**
   * @return the name to provide to {@link javax.cache.CacheManager} when creating or getting a cache
   */
  String getCacheName() {
    return name();
  }

  Duration getTimeToLive() {
    return (timeToLive == null) ? DEFAULT_TTL : timeToLive;
  }
}
