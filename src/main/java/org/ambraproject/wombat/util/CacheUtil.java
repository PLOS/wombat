package org.ambraproject.wombat.util;

import org.ambraproject.rhombat.cache.Cache;

import java.io.IOException;
import java.io.Serializable;

public class CacheUtil {
  private CacheUtil() {
  }

  @FunctionalInterface
  public static interface Lookup<T> {
    T get() throws IOException;
  }

  public static <T> T getOrCompute(Cache cache, String key, Lookup<? extends T> supplier)
      throws IOException {
    T value = cache.get(key);
    if (value == null) {
      value = supplier.get();
      cache.put(key, (Serializable) value);
    }
    return value;
  }

}
