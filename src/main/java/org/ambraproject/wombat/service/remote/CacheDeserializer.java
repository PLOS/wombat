package org.ambraproject.wombat.service.remote;

import java.io.IOException;
import java.io.InputStream;

public interface CacheDeserializer<T> {

  /**
   * Take a stream that is read in the event of a cache miss and convert it into an object that will be inserted into
   * the cache.
   *
   * @param stream the stream from a remote service
   * @return the stream contents in a cacheable form
   * @throws IOException
   */
  public abstract T call(InputStream stream) throws IOException;

}
