package org.ambraproject.wombat.service.remote;

import org.apache.http.HttpEntity;
import org.apache.http.conn.HttpClientConnectionManager;

import java.io.IOException;
import java.io.InputStream;

public class StreamService extends AbstractRemoteService<InputStream> {

  public StreamService(HttpClientConnectionManager connectionManager) {
    super(connectionManager);
  }

  @Override
  public InputStream open(HttpEntity entity) throws IOException {
    return entity.getContent();
  }

}
