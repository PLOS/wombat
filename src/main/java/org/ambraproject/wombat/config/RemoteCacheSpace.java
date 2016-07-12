package org.ambraproject.wombat.config;

import com.google.common.base.CaseFormat;

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

  private final String cacheName;
  private final Duration timeToLive;

  private RemoteCacheSpace() {
    this(null); // would be DEFAULT_TTL but static vars aren't accessible here; check in getter instead
  }

  private RemoteCacheSpace(Duration timeToLive) {
    this.timeToLive = timeToLive;
    this.cacheName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name()) + "Cache";
  }

  private static final Duration DEFAULT_TTL = new Duration(TimeUnit.HOURS, 1);

  String getCacheName() {
    return cacheName;
  }

  Duration getTimeToLive() {
    return (timeToLive == null) ? DEFAULT_TTL : timeToLive;
  }
}
