package org.ambraproject.wombat.util;

import com.google.common.base.Preconditions;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Locale;

/**
 * Debugging utility for producing HttpClients that don't care about SSL certificates. Borrowed from
 * http://stackoverflow.com/questions/2642777/trusting-all-certificates-using-httpclient-over-https
 *
 * @deprecated For use in development environments only!
 */
@Deprecated
public class TrustingHttpClient {
  private static final Logger log = LoggerFactory.getLogger(TrustingHttpClient.class);

  private TrustingHttpClient() {
    throw new AssertionError();
  }

  private static class TrustingX509TrustManager implements X509TrustManager {
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    }

    public X509Certificate[] getAcceptedIssuers() {
      return null;
    }
  }

  private static class TrustingSSLSocketFactory extends SSLSocketFactory {
    private SSLContext sslContext = SSLContext.getInstance("TLS");

    public TrustingSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
      super(truststore);
      sslContext.init(null, new TrustManager[]{new TrustingX509TrustManager()}, null);
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
      return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
      return sslContext.getSocketFactory().createSocket();
    }
  }

  /**
   * Produce a client that will accept any SSL certification, even if it is self-signed or unsigned.
   *
   * @return the client
   * @deprecated For use in development environments only!
   */
  @Deprecated
  public static CloseableHttpClient create() {
    SSLSocketFactory sf;
    try {
      KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
      trustStore.load(null, null);
      sf = new TrustingSSLSocketFactory(trustStore);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

    HttpParams params = new BasicHttpParams();
    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
    HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

    SchemeRegistry registry = new SchemeRegistry();
    registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
    registry.register(new Scheme("https", sf, 443));

    ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

    HttpClient client = new DefaultHttpClient(ccm, params);
    return new FakeCloseableHttpClient(client);
  }


  /*
   * We want to provide a CloseableHttpClient, but the way we disable SSL validation (already a kludge) doesn't.
   * Hence, a kludge within a kludge to put a mock-closeable wrapper around our hacked HttpClient object.
   * Warning: Ridiculous, mostly auto-generated code below. I'm sorry.
   * TODO: If we can't lose this class, is there at least a way to play nice with newer versions of the httpclient lib?
   */

  @Deprecated
  private static class FakeCloseableHttpClient extends CloseableHttpClient {
    private final HttpClient delegate;

    private FakeCloseableHttpClient(HttpClient delegate) {
      this.delegate = Preconditions.checkNotNull(delegate);
    }

    @Deprecated
    @Override
    public void close() throws IOException {
      if (delegate instanceof Closeable) {
        ((Closeable) delegate).close();
      } else {
        log.warn("Not actually closing FakeCloseableHttpClient");
      }
    }

    @Override
    protected CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, ClientProtocolException {
      return new FakeCloseableHttpResponse(delegate.execute(target, request, context));
    }

    @Override
    @Deprecated
    public HttpParams getParams() {
      return delegate.getParams();
    }

    @Override
    @Deprecated
    public ClientConnectionManager getConnectionManager() {
      return delegate.getConnectionManager();
    }
  }

  @Deprecated
  private static class FakeCloseableHttpResponse implements CloseableHttpResponse {
    private final HttpResponse delegate;

    private FakeCloseableHttpResponse(HttpResponse delegate) {
      this.delegate = Preconditions.checkNotNull(delegate);
    }

    @Deprecated
    @Override
    public void close() throws IOException {
      if (delegate instanceof Closeable) {
        ((Closeable) delegate).close();
      } else {
        log.warn("Not actually closing FakeCloseableHttpResponse");
      }
    }

    @Override
    public StatusLine getStatusLine() {
      return delegate.getStatusLine();
    }

    @Override
    public void setStatusLine(StatusLine statusline) {
      delegate.setStatusLine(statusline);
    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code) {
      delegate.setStatusLine(ver, code);
    }

    @Override
    public void setStatusLine(ProtocolVersion ver, int code, String reason) {
      delegate.setStatusLine(ver, code, reason);
    }

    @Override
    public void setStatusCode(int code) throws IllegalStateException {
      delegate.setStatusCode(code);
    }

    @Override
    public void setReasonPhrase(String reason) throws IllegalStateException {
      delegate.setReasonPhrase(reason);
    }

    @Override
    public HttpEntity getEntity() {
      return delegate.getEntity();
    }

    @Override
    public void setEntity(HttpEntity entity) {
      delegate.setEntity(entity);
    }

    @Override
    @Deprecated
    public Locale getLocale() {
      return delegate.getLocale();
    }

    @Override
    @Deprecated
    public void setLocale(Locale loc) {
      delegate.setLocale(loc);
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
      return delegate.getProtocolVersion();
    }

    @Override
    public boolean containsHeader(String name) {
      return delegate.containsHeader(name);
    }

    @Override
    public Header[] getHeaders(String name) {
      return delegate.getHeaders(name);
    }

    @Override
    public Header getFirstHeader(String name) {
      return delegate.getFirstHeader(name);
    }

    @Override
    public Header getLastHeader(String name) {
      return delegate.getLastHeader(name);
    }

    @Override
    public Header[] getAllHeaders() {
      return delegate.getAllHeaders();
    }

    @Override
    public void addHeader(Header header) {
      delegate.addHeader(header);
    }

    @Override
    public void addHeader(String name, String value) {
      delegate.addHeader(name, value);
    }

    @Override
    public void setHeader(Header header) {
      delegate.setHeader(header);
    }

    @Override
    public void setHeader(String name, String value) {
      delegate.setHeader(name, value);
    }

    @Override
    public void setHeaders(Header[] headers) {
      delegate.setHeaders(headers);
    }

    @Override
    public void removeHeader(Header header) {
      delegate.removeHeader(header);
    }

    @Override
    public void removeHeaders(String name) {
      delegate.removeHeaders(name);
    }

    @Override
    public HeaderIterator headerIterator() {
      return delegate.headerIterator();
    }

    @Override
    public HeaderIterator headerIterator(String name) {
      return delegate.headerIterator(name);
    }

    @Override
    @Deprecated
    public HttpParams getParams() {
      return delegate.getParams();
    }

    @Override
    @Deprecated
    public void setParams(HttpParams params) {
      delegate.setParams(params);
    }
  }

}
