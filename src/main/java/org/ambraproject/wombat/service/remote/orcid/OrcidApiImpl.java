/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.ambraproject.wombat.service.remote.orcid;

import com.google.gson.Gson;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.service.remote.RemoteService;
import org.ambraproject.wombat.service.remote.ServiceRequestException;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrcidApiImpl implements OrcidApi {

  private static final Logger log = LoggerFactory.getLogger(OrcidApiImpl.class);

  @Autowired
  private RemoteService<Reader> remoteReader;
  @Autowired
  private Gson gson;

  /**
   * {@inheritDoc}
   */
  @Override
  public String getOrcidIdFromAuthorizationCode(Site site, String code) throws IOException, URISyntaxException {

    List<NameValuePair> params = new ArrayList<>();
    params.add(new BasicNameValuePair("client_id", getOrcidClientId(site)));
    params.add(new BasicNameValuePair("client_secret", getOrcidClientSecret(site)));
    params.add(new BasicNameValuePair("grant_type", "authorization_code"));
    params.add(new BasicNameValuePair("code", code));

    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");

    URI orcidAuthUri = getOrcidAuthUri(site);

    HttpPost httpPost = new HttpPost(orcidAuthUri);
    httpPost.setEntity(entity);

    StatusLine statusLine = null;
    Map<String, Object> orcidJson = new HashMap<>();
    log.debug("ORCID API request executing: " + orcidAuthUri);
    try (CloseableHttpResponse response = remoteReader.getResponse(httpPost)) {
      statusLine = response.getStatusLine();
      String responseJson = EntityUtils.toString(response.getEntity());
      orcidJson = gson.fromJson(responseJson, HashMap.class);
    } catch (ServiceRequestException e) {
      handleOrcidException(e);
    } finally {
      httpPost.releaseConnection();
    }

    if (statusLine != null && statusLine.getStatusCode() != HttpStatus.OK.value()) {
      throw new RuntimeException("bad response from ORCID authorization API: " + statusLine);
    }

    return String.valueOf(orcidJson.get("orcid"));
  }

  private void handleOrcidException(ServiceRequestException e)
      throws OrcidAuthenticationTokenReusedException, OrcidAuthenticationTokenExpiredException {
    Map<String, String> errorJson = gson.fromJson(e.getResponseBody(), HashMap.class);
    final String errorDescription = errorJson.get("error_description");
    if (errorDescription.startsWith("Reused authorization code: ")) {
      throw new OrcidAuthenticationTokenReusedException();
    } else if (errorDescription.contains("expired")) {
      throw new OrcidAuthenticationTokenExpiredException();
    } else {
      log.error("Unknown error from ORCID authorization API: " + errorDescription);
      throw new RuntimeException(e);
    }
  }

  private URI getOrcidAuthUri(Site site) throws URISyntaxException {
    final String host = (String) site.getTheme().getConfigMap("orcid").get("host");
    return new URI(host + "/oauth/token");
  }

  private String getOrcidClientId(Site site) throws URISyntaxException {
    return (String) site.getTheme().getConfigMap("orcid").get("clientId");
  }

  private String getOrcidClientSecret(Site site) throws URISyntaxException {
    return (String) site.getTheme().getConfigMap("orcid").get("clientSecret");
  }

}
