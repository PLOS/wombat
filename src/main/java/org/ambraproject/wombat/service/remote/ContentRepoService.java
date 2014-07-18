package org.ambraproject.wombat.service.remote;

import com.google.common.base.Optional;
import org.apache.http.Header;

import java.io.IOException;
import java.io.Reader;

public interface ContentRepoService {

  public abstract AssetServiceResponse request(String key, Optional<Integer> version, Header... headers)
      throws IOException;

  public abstract <T> T requestCachedReader(String cacheKey, String key, Optional<Integer> version, CacheDeserializer<Reader, T> callback) throws IOException;

}
