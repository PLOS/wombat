package org.ambraproject.wombat.service;

import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;
import com.google.common.io.Closer;
import com.google.gson.Gson;
import org.ambraproject.wombat.config.SoaConfiguration;
import org.ambraproject.wombat.util.TrustingHttpClient;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.beans.factory.annotation.Autowired;

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
  private SoaConfiguration soaConfiguration;
  @Autowired
  private Gson gson;

  @Override
  public InputStream requestStream(String address) throws IOException {
    URI targetUri;
    try {
      targetUri = new URL(soaConfiguration.getServer(), Preconditions.checkNotNull(address)).toURI();
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(e);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }

    HttpClient client = ("https".equals(targetUri.getScheme()) && soaConfiguration.trustUnsignedServer())
        ? TrustingHttpClient.create() : new DefaultHttpClient();
    HttpGet get = new HttpGet(targetUri);
    HttpResponse response = client.execute(get);
    HttpEntity entity = response.getEntity();
    if (entity == null) {
      throw new RuntimeException("No response");
    }
    return entity.getContent();
  }

  @Override
  public String requestString(String address) throws IOException {
    String response;
    InputStream stream = null;
    boolean threw = true;
    try {
      stream = requestStream(address);
      response = IOUtils.toString(stream);
      threw = false;
    } finally {
      Closeables.close(stream, threw);
    }
    return response;
  }

  @Override
  public <T> T requestObject(String address, Class<T> responseClass) throws IOException {
    Preconditions.checkNotNull(responseClass);

    T responseObject;
    Closer closer = Closer.create();
    Reader reader;
    InputStream responseStream;
    try {
      responseStream = closer.register(requestStream(address));
      reader = closer.register(new InputStreamReader(responseStream));
      responseObject = gson.fromJson(reader, responseClass);
    } finally {
      closer.close();
    }
    return responseObject;
  }

}
