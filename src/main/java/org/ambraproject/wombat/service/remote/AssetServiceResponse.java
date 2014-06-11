package org.ambraproject.wombat.service.remote;

import com.google.common.base.Preconditions;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.springframework.http.HttpStatus;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Encapsulates a response from a remote service that provides an asset.
 * <p/>
 * Usually just wraps around an {@link org.apache.http.HttpResponse}, but is also useful for simulating a response from
 * an asset read from a local file in dev mode.
 */
public abstract class AssetServiceResponse implements Closeable {

  private AssetServiceResponse() {
  }

  public abstract InputStream openStream() throws IOException;

  public abstract int getStatusCode();

  public abstract Header[] getAllHeaders();


  public static AssetServiceResponse wrap(CloseableHttpResponse response) {
    return new ResponseWrapper(response);
  }

  /**
   * Decorate a genuine response.
   */
  private static class ResponseWrapper extends AssetServiceResponse {
    private final CloseableHttpResponse delegate;

    private ResponseWrapper(CloseableHttpResponse delegate) {
      this.delegate = Preconditions.checkNotNull(delegate);
    }

    @Override
    public InputStream openStream() throws IOException {
      return delegate.getEntity().getContent();
    }

    @Override
    public int getStatusCode() throws IllegalStateException {
      return delegate.getStatusLine().getStatusCode();
    }

    @Override
    public Header[] getAllHeaders() {
      return delegate.getAllHeaders();
    }

    @Override
    public void close() throws IOException {
      delegate.close();
    }
  }


  public static AssetServiceResponse wrap(File file) {
    return new FileWrapper(file);
  }

  /**
   * Mock an HTTP response.
   */
  private static class FileWrapper extends AssetServiceResponse {
    private final File file;
    private InputStream stream;

    private FileWrapper(File file) {
      this.file = Preconditions.checkNotNull(file);
    }

    @Override
    public InputStream openStream() throws IOException {
      return (stream = new FileInputStream(file));
    }

    @Override
    public int getStatusCode() {
      return ((stream != null || file.exists()) ? HttpStatus.OK : HttpStatus.NOT_FOUND).value();
    }

    @Override
    public Header[] getAllHeaders() {
      return new Header[0];
    }

    @Override
    public void close() throws IOException {
      if (stream != null) {
        stream.close();
      }
    }
  }

}
