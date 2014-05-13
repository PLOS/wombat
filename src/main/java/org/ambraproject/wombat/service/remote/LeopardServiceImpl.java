package org.ambraproject.wombat.service.remote;

import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.util.UriUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;

public class LeopardServiceImpl implements LeopardService {

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;
  @Autowired
  private CachedRemoteService<Reader> cachedRemoteReader;

  @Override
  public Reader readHtml(String path) throws IOException {
    // TODO: Cache
    URI address = UriUtil.concatenate(runtimeConfiguration.getLeopardServer(), path);
    return cachedRemoteReader.request(address);
  }

}
