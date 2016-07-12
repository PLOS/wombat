package org.ambraproject.wombat.service.remote;

import com.google.common.collect.ImmutableList;
import org.ambraproject.wombat.config.RemoteCacheSpace;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Cache key for a value returned by a remote service.
 */
public final class RemoteCacheKey implements Serializable {

  private final RemoteCacheSpace remoteCacheSpace;
  private final ImmutableList<String> identifiers;

  private RemoteCacheKey(RemoteCacheSpace remoteCacheSpace, List<String> identifiers) {
    this.remoteCacheSpace = Objects.requireNonNull(remoteCacheSpace);
    this.identifiers = ImmutableList.copyOf(identifiers);
  }

  /**
   * Create a cache key.
   *
   * @param remoteCacheSpace identifies the cache to use
   * @param identifier       a string that uniquely identifies the value to be cached
   * @return a cache key
   */
  public static RemoteCacheKey create(RemoteCacheSpace remoteCacheSpace, String identifier) {
    return new RemoteCacheKey(remoteCacheSpace, ImmutableList.of(identifier));
  }

  /**
   * Create a cache key from a sequence of strings that uniquely identifies the value to be cached.
   *
   * @param remoteCacheSpace identifies the cache to use
   * @return a cache key
   */
  public static RemoteCacheKey create(RemoteCacheSpace remoteCacheSpace, String firstIdentifier, String secondIdentifier, String... moreIdentifiers) {
    ImmutableList<String> identifiers = ImmutableList.<String>builder()
        .add(firstIdentifier).add(secondIdentifier).add(moreIdentifiers).build();
    return new RemoteCacheKey(remoteCacheSpace, identifiers);
  }

  public RemoteCacheSpace getRemoteCacheSpace() {
    return remoteCacheSpace;
  }

  @Override
  public boolean equals(Object o) {
    return this == o || o != null && getClass() == o.getClass()
        && remoteCacheSpace == ((RemoteCacheKey) o).remoteCacheSpace
        && identifiers.equals(((RemoteCacheKey) o).identifiers);
  }

  @Override
  public int hashCode() {
    return 31 * remoteCacheSpace.hashCode() + identifiers.hashCode();
  }
}
