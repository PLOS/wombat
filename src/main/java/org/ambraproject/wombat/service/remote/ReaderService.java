package org.ambraproject.wombat.service.remote;

import org.apache.commons.io.Charsets;
import org.apache.http.HttpEntity;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Objects;

public class ReaderService extends AbstractRemoteService<Reader> {
  private static final Logger log = LoggerFactory.getLogger(ReaderService.class);

  public ReaderService(HttpClientConnectionManager connectionManager) {
    super(connectionManager);
  }

  /*
   * Default for converting the response's byte stream to characters.
   * Preferred over java.nio.charset.Charset.defaultCharset() to avoid an environmental dependency.
   */
  private static final Charset DEFAULT_CHARSET = Charsets.UTF_8;

  private static Charset getCharsetOrDefault(HttpEntity entity) {
    Charset charset = null;
    ContentType contentType = ContentType.get(Objects.requireNonNull(entity));
    if (contentType != null) {
      charset = contentType.getCharset(); // may be null
    }
    if (charset == null) {
      log.warn("Charset not specified in response header; defaulting to {}", DEFAULT_CHARSET.name());
      charset = DEFAULT_CHARSET;
    }
    return charset;
  }

  @Override
  public Reader open(HttpEntity entity) throws IOException {
    Charset charset = getCharsetOrDefault(entity);
    return new InputStreamReader(entity.getContent(), charset);
  }

}
