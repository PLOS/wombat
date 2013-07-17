package org.ambraproject.wombat.service;

import com.google.common.base.Preconditions;
import com.google.common.io.Closer;
import com.google.gson.Gson;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.util.TrustingHttpClient;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class SoaServiceImpl implements SoaService {

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;
  @Autowired
  private Gson gson;

  @Override
  public InputStream requestStream(String address) throws IOException {
    URI targetUri;
    try {
      targetUri = new URL(runtimeConfiguration.getServer(), Preconditions.checkNotNull(address)).toURI();
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(e);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }

    HttpClient client = (runtimeConfiguration.trustUnsignedServer() && "https".equals(targetUri.getScheme()))
        ? TrustingHttpClient.create() : new DefaultHttpClient();
    HttpGet get = new HttpGet(targetUri);

    HttpResponse response = client.execute(get);
    StatusLine statusLine = response.getStatusLine();
    if (statusLine.getStatusCode() >= 400) {
      if (statusLine.getStatusCode() == 404) {
        throw new EntityNotFoundException(address);
      } else {
        String message = String.format("Request to \"%s\" failed (%d): %s",
            address, statusLine.getStatusCode(), statusLine.getReasonPhrase());
        throw new RuntimeException(message);
      }
    }

    HttpEntity entity = response.getEntity();
    if (entity == null) {
      throw new RuntimeException("No response");
    }
    return entity.getContent();
  }

  @Override
  public String requestString(String address) throws IOException {
    Closer closer = Closer.create();
    try {
      InputStream stream = closer.register(requestStream(address));
      return IOUtils.toString(stream); // buffered
    } catch (Throwable t) {
      throw closer.rethrow(t);
    } finally {
      closer.close();
    }
  }

  @Override
  public <T> T requestObject(String address, Class<T> responseClass) throws IOException {
    Preconditions.checkNotNull(responseClass);
    Closer closer = Closer.create();
    try {
      InputStream stream = closer.register(new BufferedInputStream(requestStream(address)));
      Reader reader = closer.register(new InputStreamReader(stream));
      return gson.fromJson(reader, responseClass);
    } catch (Throwable t) {
      throw closer.rethrow(t);
    } finally {
      closer.close();
    }
  }

}
