package org.ambraproject.wombat.service.remote;

import org.apache.http.HttpEntity;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

public class ReaderService extends AbstractRemoteService<Reader> {

  public ReaderService(HttpClientConnectionManager connectionManager) {
    super(connectionManager);
  }

  @Override
  public Reader open(HttpEntity entity) throws IOException {
    Charset charset = ContentType.getOrDefault(entity).getCharset();
    return new InputStreamReader(entity.getContent(), charset);
  }

}
