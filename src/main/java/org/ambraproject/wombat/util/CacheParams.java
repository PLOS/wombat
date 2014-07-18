package org.ambraproject.wombat.util;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

public class CacheParams {

  private final String cacheKey;
  private final Optional<Integer> timeToLive;

  private CacheParams(String cacheKey, Optional<Integer> timeToLive) {
    this.cacheKey = Preconditions.checkNotNull(cacheKey);
    this.timeToLive = timeToLive;
  }

  public static CacheParams create(String cacheKey) {
    return new CacheParams(cacheKey, Optional.<Integer>absent());
  }

  public static CacheParams create(String cacheKey, Integer timeToLive) {
    return new CacheParams(cacheKey, Optional.fromNullable(timeToLive));
  }

  public Optional<Integer> getTimeToLive() {
    return timeToLive;
  }

  public String getCacheKey() {
    return cacheKey;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CacheParams that = (CacheParams) o;

    if (!cacheKey.equals(that.cacheKey)) return false;
    if (!timeToLive.equals(that.timeToLive)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = cacheKey.hashCode();
    result = 31 * result + timeToLive.hashCode();
    return result;
  }
}
