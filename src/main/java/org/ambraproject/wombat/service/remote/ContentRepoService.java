package org.ambraproject.wombat.service.remote;

import com.google.common.base.Optional;
import org.ambraproject.wombat.util.CacheParams;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

public interface ContentRepoService {

  public abstract CloseableHttpResponse request(String key, Optional<Integer> version, Collection<? extends Header> headers)
      throws IOException;

  public abstract <T> T requestCachedReader(CacheParams cacheParams, String key, Optional<Integer> version, CacheDeserializer<Reader, T> callback) throws IOException;

}
