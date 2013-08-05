package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class SoaServiceImpl extends JsonService implements SoaService {

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  @Override
  public InputStream requestStream(String address) throws IOException {
    return requestStream(buildUri(address));
  }

  @Override
  public <T> T requestObject(String address, Class<T> responseClass) throws IOException {
    return requestObject(buildUri(address), responseClass);
  }

  private URI buildUri(String address) {
    return buildUri(runtimeConfiguration.getServer(), address);
  }
}
