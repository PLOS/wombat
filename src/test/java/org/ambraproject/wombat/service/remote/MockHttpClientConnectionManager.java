package org.ambraproject.wombat.service.remote;

import org.apache.http.*;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HttpContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by jkrzemien on 7/15/14.
 */

public class MockHttpClientConnectionManager implements HttpClientConnectionManager {
	private HttpResponse response;
	
	public void setResponse(int code, String body) {
		this.response = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), code, ""));
		BasicHttpEntity entity = new BasicHttpEntity();
		if (body != null) {
			entity.setContent(new ByteArrayInputStream(body.getBytes()));
			this.response.setEntity(entity);
		}
	}
	
	@Override
	public ConnectionRequest requestConnection(HttpRoute paramHttpRoute, Object paramObject) {
		return new ConnectionRequest() {
			
			@Override
			public boolean cancel() {
				return false;
			}
			
			@Override
			public HttpClientConnection get(long paramLong, TimeUnit paramTimeUnit)
					throws InterruptedException, ExecutionException,
					ConnectionPoolTimeoutException {
				return new HttpClientConnection(){

					@Override
					public void close() throws IOException {
					}

					@Override
					public boolean isOpen() {
						return true;
					}

					@Override
					public boolean isStale() {
						return false;
					}

					@Override
					public void setSocketTimeout(int paramInt) {
					}

					@Override
					public int getSocketTimeout() {
						return 0;
					}

					@Override
					public void shutdown() throws IOException {
					}

					@Override
					public HttpConnectionMetrics getMetrics() {
						return null;
					}

					@Override
					public boolean isResponseAvailable(int paramInt) throws IOException {
						return false;
					}

					@Override
					public void sendRequestHeader(HttpRequest paramHttpRequest)	throws HttpException, IOException {
						if (response != null) {
							for (Header header : paramHttpRequest.getAllHeaders()) {
								response.addHeader(header);
							}
						}
					}

					@Override
					public void sendRequestEntity(HttpEntityEnclosingRequest paramHttpEntityEnclosingRequest) throws HttpException, IOException {
					}

					@Override
					public HttpResponse receiveResponseHeader()	throws HttpException, IOException {
						return response;
					}

					@Override
					public void receiveResponseEntity(HttpResponse paramHttpResponse) throws HttpException, IOException {
					}

					@Override
					public void flush() throws IOException {
					}
				};
			}
		};
	}

	@Override
	public void releaseConnection(
			HttpClientConnection paramHttpClientConnection, Object paramObject,
			long paramLong, TimeUnit paramTimeUnit) {
	}

	@Override
	public void connect(HttpClientConnection paramHttpClientConnection,
			HttpRoute paramHttpRoute, int paramInt, HttpContext paramHttpContext)
			throws IOException {
	}

	@Override
	public void upgrade(HttpClientConnection paramHttpClientConnection,
			HttpRoute paramHttpRoute, HttpContext paramHttpContext)
			throws IOException {
	}

	@Override
	public void routeComplete(HttpClientConnection paramHttpClientConnection,
			HttpRoute paramHttpRoute, HttpContext paramHttpContext)
			throws IOException {
	}

	@Override
	public void closeIdleConnections(long paramLong, TimeUnit paramTimeUnit) {
	}

	@Override
	public void closeExpiredConnections() {
	}

	@Override
	public void shutdown() {
	}
}
