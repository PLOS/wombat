package org.ambraproject.wombat.service.remote;

import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;

public class LeopardServiceImpl extends RemoteService implements LeopardService {

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  @Override
  public InputStream requestStream(String path) throws IOException {
    return requestStream(buildUri(runtimeConfiguration.getLeopardServer(), path));
  }

}
