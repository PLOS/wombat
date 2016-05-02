package org.ambraproject.wombat.service.remote;

import org.ambraproject.wombat.util.CacheParams;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.OptionalInt;

public interface ContentApi {

  /**
   * Requests a file from the content repository. Returns the full response.
   *
   * @param key     content repo key
   * @param version content repo version
   * @return the response from the content repo
   * @throws IOException
   * @throws org.ambraproject.wombat.service.EntityNotFoundException if the repository does not provide the file
   */
  public abstract CloseableHttpResponse request(String key, OptionalInt version, Collection<? extends Header> headers)
      throws IOException;

  public abstract Map<String, Object> requestMetadata(CacheParams cacheParams, String key, OptionalInt version) throws IOException;

}
