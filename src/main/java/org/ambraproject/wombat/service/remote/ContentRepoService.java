package org.ambraproject.wombat.service.remote;

import com.google.common.base.Optional;
import org.apache.http.Header;

import java.io.IOException;

public interface ContentRepoService {

  public abstract AssetServiceResponse request(String key, Optional<Integer> version, Header... headers)
      throws IOException;

}
