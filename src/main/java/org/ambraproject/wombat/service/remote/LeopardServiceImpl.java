package org.ambraproject.wombat.service.remote;

import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Reader;

public class LeopardServiceImpl extends RemoteService implements LeopardService {

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  @Override
  public Reader readHtml(String path) throws IOException {
    return requestReader(buildUri(runtimeConfiguration.getLeopardServer(), path));
  }

}
