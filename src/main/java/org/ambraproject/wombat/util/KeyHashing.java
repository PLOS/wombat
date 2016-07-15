package org.ambraproject.wombat.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Utilities for representing unique keys and values with collision-proof hash functions.
 */
public class KeyHashing {
  private KeyHashing() {
    throw new AssertionError("Not instantiable");
  }

  @VisibleForTesting
  static final HashFunction HASH_ALGORITHM = Hashing.sha1();
  @VisibleForTesting
  static final BaseEncoding HASH_BASE = BaseEncoding.base32();
  @VisibleForTesting
  static final Charset HASH_CHARSET = Charsets.UTF_8;

  /**
   * Create a hash from the sequence of identifiers and return it for use as a cache key.
   * <p>
   * This is useful because the strings may be too long, or may contain characters that are invalid, for a cache key or
   * file name.
   *
   * @param identifiers a sequence of identifiers that uniquely identify a cache value
   * @return a digest of bounded length
   */
  public static String createKeyHash(List<String> identifiers) {
    Hasher hasher = HASH_ALGORITHM.newHasher();
    for (String identifier : identifiers) {
      byte[] value = identifier.getBytes(HASH_CHARSET);
      hasher.putInt(value.length); // to prevent collisions between, e.g., ["ab", c"] and ["a", "bc"]
      hasher.putBytes(value);
    }
    return HASH_BASE.encode(hasher.hash().asBytes());
  }

  /**
   * Create a hash from raw byte content and return it as a string for use as a content-based identifier.
   *
   * @param value raw bytes
   * @return a hash digest of the bytes
   */
  public static String createContentHash(byte[] value) {
    return HASH_BASE.encode(HASH_ALGORITHM.hashBytes(value).asBytes());
  }

}
