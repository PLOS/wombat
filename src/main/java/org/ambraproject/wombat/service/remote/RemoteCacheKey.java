package org.ambraproject.wombat.service.remote;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

/**
 * Cache key for a value returned by a remote service.
 */
public final class RemoteCacheKey {

  private final String namespace;
  private final ImmutableList<String> identifiers;

  private RemoteCacheKey(String namespace, List<String> identifiers) {
    this.namespace = Objects.requireNonNull(namespace);
    this.identifiers = ImmutableList.copyOf(identifiers);
  }

  /**
   * Create a cache key.
   *
   * @param namespace  a constant string that identifies the namespace of identifiers in use
   * @param identifier a string that uniquely identifies the value to be cached
   * @return a cache key
   */
  public static RemoteCacheKey create(String namespace, String identifier) {
    return new RemoteCacheKey(namespace, ImmutableList.of(identifier));
  }

  /**
   * Create a cache key from a sequence of strings that uniquely identifies the value to be cached.
   *
   * @param namespace a constant string that identifies the namespace of identifiers in use
   * @return a cache key
   */
  public static RemoteCacheKey create(String namespace, String firstIdentifier, String secondIdentifier, String... moreIdentifiers) {
    ImmutableList<String> identifiers = ImmutableList.<String>builder()
        .add(firstIdentifier).add(secondIdentifier).add(moreIdentifiers).build();
    return new RemoteCacheKey(namespace, identifiers);
  }

  @Override
  public boolean equals(Object o) {
    return this == o || o != null && getClass() == o.getClass()
        && namespace.equals(((RemoteCacheKey) o).namespace)
        && identifiers.equals(((RemoteCacheKey) o).identifiers);
  }

  @Override
  public int hashCode() {
    return 31 * identifiers.hashCode() + namespace.hashCode();
  }
}
