/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.ambraproject.wombat.util;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CacheKey {

  private static final Logger log = LoggerFactory.getLogger(CacheKey.class);

  static final HashFunction HASH_ALGORITHM = Hashing.sha1();
  static final Charset HASH_CHARSET = Charsets.UTF_8;
  static final BaseEncoding HASH_BASE = BaseEncoding.base32();

  private final Optional<Integer> timeToLive;
  private final String prefix;
  private final ImmutableList<String> identifiers;

  private CacheKey(Integer timeToLive, String prefix, List<String> identifiers) {
    this.timeToLive = Optional.ofNullable(timeToLive);
    this.prefix = Objects.requireNonNull(prefix);
    this.identifiers = ImmutableList.copyOf(identifiers);
  }

  /**
   * Create a cache key.
   *
   * @param prefix     a constant string that identifies the namespace of identifiers in use
   * @param identifier a string that uniquely identifies the value to be cached
   * @return a cache key
   */
  public static CacheKey create(String prefix, String identifier) {
    return new CacheKey(null, prefix, ImmutableList.of(identifier));
  }

  /**
   * Create a cache key from a sequence of strings that uniquely identifies the value to be cached.
   *
   * @param prefix a constant string that identifies the namespace of identifiers in use
   * @return a cache key
   */
  public static CacheKey create(String prefix, String firstIdentifier, String secondIdentifier, String... moreIdentifiers) {
    ImmutableList<String> identifiers = ImmutableList.<String>builder()
        .add(firstIdentifier).add(secondIdentifier).add(moreIdentifiers).build();
    return new CacheKey(null, prefix, identifiers);
  }

  /**
   * @param timeToLive a time-to-live parameter for the cache
   * @return a copy of this key with the time-to-live parameter added
   * @throws IllegalStateException if this key already has a time to live
   */
  public CacheKey addTimeToLive(int timeToLive) {
    if (this.timeToLive.isPresent()) {
      throw new IllegalStateException("timeToLive already set");
    }
    return new CacheKey(timeToLive, prefix, identifiers);
  }

  public Optional<Integer> getTimeToLive() {
    return timeToLive;
  }

  /**
   * @return a string that corresponds uniquely to this object and can be passed to an external cache as a key.
   */
  public String getExternalKey() {
    return prefix + ":" + createKeyHash(identifiers);
  }

  private static final char HASH_SEPARATOR = '\0';

  /**
   * Create a hash from the sequence of identifiers and return it, with a prefix appended, for use as a cache key.
   * <p>
   * This is useful because the strings may contain characters that are invalid for a cache key. Also, when they are
   * concatenated, they may be too long (by environmental default, >250 chars) to be used as a key.
   *
   * @param identifiers a sequence of identifiers that uniquely identify a cache value
   * @return a digest of bounded length
   */
  public static String createKeyHash(List<String> identifiers) {
    Hasher hasher = HASH_ALGORITHM.newHasher();
    for (Iterator<String> iterator = identifiers.iterator(); iterator.hasNext(); ) {
      hasher.putString(iterator.next(), HASH_CHARSET);
      if (iterator.hasNext()) {
        hasher.putChar(HASH_SEPARATOR);
      }
    }
    return HASH_BASE.encode(hasher.hash().asBytes());
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

    CacheKey cacheKey = (CacheKey) o;

    if (!timeToLive.equals(cacheKey.timeToLive)) return false;
    if (!prefix.equals(cacheKey.prefix)) return false;
    return identifiers.equals(cacheKey.identifiers);

  }

  @Override
  public int hashCode() {
    int result = timeToLive.hashCode();
    result = 31 * result + prefix.hashCode();
    result = 31 * result + identifiers.hashCode();
    return result;
  }
}
