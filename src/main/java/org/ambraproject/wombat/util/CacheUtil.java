package org.ambraproject.wombat.util;


import javax.cache.Cache;
import java.io.IOException;
import java.io.Serializable;

public class CacheUtil {
  private CacheUtil() {
    throw new AssertionError("Not instantiable");
  }

  /**
   * Function to look up a value if it is not cached.
   */
  @FunctionalInterface
  public static interface Lookup<T> {
    T get() throws IOException;
  }

  /**
   * Retrieve a value from a cache if possible, computing and inserting it otherwise.
   * <p>
   * The value returned by the {@code lookup} must be {@link Serializable}, even though the type {@code T} is not
   * required to implement {@code Serializable}. (For example, {@code T} may be {@link java.util.List}, which is not
   * {@code Serializable}, provided that {@code lookup} guarantees that all returned values will be {@link
   * java.util.ArrayList} instances, which are {@code Serializable}.)
   *
   * @param cache  the cache
   * @param key    the cache key to look up
   * @param lookup the operation that will produce the value if it is not in the cache
   * @return the retrieved or produced value
   * @throws IOException
   * @throws java.lang.ClassCastException if {@code lookup} returns a non-{@code Serializable} value
   */
  public static <K, T> T getOrCompute(Cache<K, T> cache, K key, Lookup<? extends T> lookup)
      throws IOException {
    T value = cache != null ? cache.get(key) : null;
    if (value == null) {
      value = lookup.get();
      if (cache != null) {
        cache.put(key, value);
      }
    }
    return value;
  }

}
