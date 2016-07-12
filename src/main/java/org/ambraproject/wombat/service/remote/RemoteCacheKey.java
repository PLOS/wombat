package org.ambraproject.wombat.service.remote;

import com.google.common.collect.ImmutableList;

import javax.cache.Cache;
import java.util.List;
import java.util.Objects;

/**
 * Cache key for a value returned by a remote service.
 */
public final class RemoteCacheKey {

  private final Cache<RemoteCacheKey, Object> cache;
  private final ImmutableList<String> identifiers;

  private RemoteCacheKey(Cache<RemoteCacheKey, Object> cache, List<String> identifiers) {
    this.cache = Objects.requireNonNull(cache);
    this.identifiers = ImmutableList.copyOf(identifiers);
  }

  /**
   * Create a cache key.
   *
   * @param cache      the cache to use
   * @param identifier a string that uniquely identifies the value to be cached
   * @return a cache key
   */
  public static RemoteCacheKey create(Cache<RemoteCacheKey, Object> cache, String identifier) {
    return new RemoteCacheKey(cache, ImmutableList.of(identifier));
  }

  /**
   * Create a cache key from a sequence of strings that uniquely identifies the value to be cached.
   *
   * @param cache the cache to use
   * @return a cache key
   */
  public static RemoteCacheKey create(Cache<RemoteCacheKey, Object> cache, String firstIdentifier, String secondIdentifier, String... moreIdentifiers) {
    ImmutableList<String> identifiers = ImmutableList.<String>builder()
        .add(firstIdentifier).add(secondIdentifier).add(moreIdentifiers).build();
    return new RemoteCacheKey(cache, identifiers);
  }

  Cache<RemoteCacheKey, Object> getCache() {
    return cache;
  }

  // IMPORTANT: Only `identifiers` is used for this class's value identity.
  // Do NOT reference `cache` in hashCode and equals.

  @Override
  public boolean equals(Object o) {
    return this == o || o != null && getClass() == o.getClass()
        && identifiers.equals(((RemoteCacheKey) o).identifiers);
  }

  @Override
  public int hashCode() {
    return identifiers.hashCode();
  }
}
