package org.ambraproject.wombat.util;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;

public class CacheParams {

  private static final Logger log = LoggerFactory.getLogger(CacheParams.class);
  private final String cacheKey;
  private final Optional<Integer> timeToLive;
  public static final String HASH_ALGORITHM = "SHA-256"; // other supported options are "MD5" and "SHA-1"

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

  // create a hash from a given string and return it as a hex string for use as a cache key
  // this is useful when a string value may be too long (>250 chars) to be used directly as a key
  public static String createKeyHash(String value) {
    try {
      MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
      byte[] hashed = md.digest(value.getBytes("UTF-8"));
      StringBuffer stringBuffer = new StringBuffer();
      for (int i = 0; i < hashed.length; i++) {
        stringBuffer.append(Integer.toString((hashed[i] & 0xff) + 0x100, 16)
                .substring(1));
      }
      return stringBuffer.toString();
    } catch (Exception e) {
      log.error(String.format("Error generating cache key hash. Using unmodified string value: %s", value), e);
      return value;
    }
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
