package org.ambraproject.wombat.service.remote;

import java.io.Closeable;
import java.io.IOException;

/**
 * How to convert a data stream into a cacheable value.
 *
 * @param <S> the type of stream (typically {@link java.io.InputStream} or {@link java.io.Reader})
 * @param <T> the type of data
 */
@FunctionalInterface
public interface CacheDeserializer<S extends Closeable, T> {

  /**
   * Take a stream that is read in the event of a cache miss and convert it into an object that will be inserted into
   * the cache.
   *
   * @param stream the stream from a remote service
   * @return the stream contents in a cacheable form
   * @throws IOException
   */
  public abstract T read(S stream) throws IOException;

}
