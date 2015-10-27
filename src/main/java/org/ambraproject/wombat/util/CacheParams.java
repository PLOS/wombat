package org.ambraproject.wombat.util;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;

public class CacheParams {

  private static final Logger log = LoggerFactory.getLogger(CacheParams.class);
  private final String cacheKey;
  private final Optional<Integer> timeToLive;

  static final HashFunction HASH_ALGORITHM = Hashing.sha1();
  static final Charset HASH_CHARSET = Charsets.UTF_8;
  static final BaseEncoding HASH_BASE = BaseEncoding.base32();

  private CacheParams(String cacheKey, Optional<Integer> timeToLive) {
    this.cacheKey = Objects.requireNonNull(cacheKey);
    this.timeToLive = Objects.requireNonNull(timeToLive);
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
   * Create a hash from a given string and return it as a string for use as a cache key. This is useful when a string
   * value may be too long (by environmental default, >250 chars) to be used directly as a key.
   *
   * @param value a potentially long key string
   * @return a digest of bounded length
   */
  public static String createKeyHash(String value) {
    return createContentHash(value.getBytes(HASH_CHARSET));
  }

  private static final char HASH_TERMINATOR = '\0';

  /**
   * Create a hash from a sequence of strings and return it as a string for use as a cache key. This is useful when the
   * strings, concatenated, may be too long (by environmental default, >250 chars) to be used directly as a key.
   * <p>
   * For consistent hashes, the argument must have a well-defined iteration order. Passing in, for example, a {@link
   * java.util.HashSet} is a bad idea.
   *
   * @param values a potentially long sequence of key strings
   * @return a digest of bounded length
   */
  public static String createKeyHash(Iterable<String> values) {
    Hasher hasher = HASH_ALGORITHM.newHasher();
    for (String value : values) {
      hasher.putString(value, HASH_CHARSET);
      hasher.putChar(HASH_TERMINATOR);
    }
    return HASH_BASE.encode(hasher.hash().asBytes());
  }

  /**
   * Create a hash from a sequence of strings and return it as a string for use as a cache key.
   *
   * @return a digest of bounded length
   */
  public static String createKeyHash(String first, String second, String... more) {
    return createKeyHash(Iterables.concat(ImmutableList.of(first, second), Arrays.asList(more)));
  }

  /**
   * Create a hash from raw byte content and return it as a string for use as a cache key or content-based identifier.
   *
   * @param value raw bytes
   * @return a hash digest of the bytes
   */
  public static String createContentHash(byte[] value) {
    return HASH_BASE.encode(HASH_ALGORITHM.hashBytes(value).asBytes());
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
