package org.ambraproject.wombat.util;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.security.MessageDigest;

public class CacheParams {

  private static final Logger log = LoggerFactory.getLogger(CacheParams.class);
  private final String cacheKey;
  private final Optional<Integer> timeToLive;
  public static final HashFunction HASH_ALGORITHM = Hashing.sha256();
  public static final Charset HASH_CHARSET = Charsets.UTF_8;

  private CacheParams(String cacheKey, Optional<Integer> timeToLive) {
    this.cacheKey = Preconditions.checkNotNull(cacheKey);
    this.timeToLive = Preconditions.checkNotNull(timeToLive);
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

  /**
   * Create a hash from a given string and return it as a hex string for use as a cache key
   * This is useful when a string value may be too long (>250 chars) to be used directly as a key
   * @param value
   * @return
   */
  public static String createKeyHash(String value) {
      return BaseEncoding.base16().encode(HASH_ALGORITHM.hashString(value, HASH_CHARSET).asBytes());
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
