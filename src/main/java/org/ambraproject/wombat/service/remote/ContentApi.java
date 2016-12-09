package org.ambraproject.wombat.service.remote;

import org.ambraproject.wombat.util.CacheKey;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public interface ContentApi {

  /**
   * Requests a file from the content repository. Returns the full response.
   *
   * @param key     content repo key
   * @return the response from the content repo
   * @throws IOException
   * @throws org.ambraproject.wombat.service.EntityNotFoundException if the repository does not provide the file
   */
  public abstract CloseableHttpResponse request(ContentKey key, Collection<? extends Header> headers)
      throws IOException;

  public abstract Map<String, Object> requestMetadata(CacheKey cacheKey, ContentKey key) throws IOException;

}
