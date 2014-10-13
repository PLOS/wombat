package org.ambraproject.wombat.util;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.remote.ServiceRequestException;
import org.apache.commons.io.IOUtils;
import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
  A utility class for creation and management of HTTP messages
 */
public class HttpMessageUtil {

  /**
   * Names of headers that, on a request from the client, should be passed through on our request to the service
   * tier (Rhino or Content Repo).
   */
  protected static final ImmutableSet<String> REQUEST_HEADER_WHITELIST = caseInsensitiveImmutableSet("X-Proxy-Capabilities");

  /**
   * Names of headers that, in a response from the service tier (Rhino or Content Repo), should be passed through
   * to the client.
   */
  protected static final ImmutableSet<String> RESPONSE_HEADER_WHITELIST = caseInsensitiveImmutableSet(
          HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_DISPOSITION, "X-Reproxy-URL", "X-Reproxy-Cache-For");


  // Inconsistent with equals. See Javadoc for java.util.SortedSet.
  private static ImmutableSortedSet<String> caseInsensitiveImmutableSet(String... strings) {
    return ImmutableSortedSet.copyOf(String.CASE_INSENSITIVE_ORDER, Arrays.asList(strings));
  }


  /**
   * Copy content with whitelisted headers between responses
   *
   * @param response
   * @param responseTo
   */
  public static void copyResponseWithHeaders(CloseableHttpResponse responseFrom, HttpServletResponse responseTo)
      throws IOException {
    for (Header header : responseFrom.getAllHeaders()) {
      if (RESPONSE_HEADER_WHITELIST.contains(header.getName())) {
        responseTo.setHeader(header.getName(), header.getValue());
      }
    }

    try (InputStream streamFromService = responseFrom.getEntity().getContent();
         OutputStream streamToClient = responseTo.getOutputStream()) {
      IOUtils.copy(streamFromService, streamToClient);
    }
  }


  /**
   * Return a list of headers from a request, using an optional whitelist
   *
   * @param request a request
   * @return its headers
   */
  public static Header[] getRequestHeaders(HttpServletRequest request) {
    Enumeration headerNames = request.getHeaderNames();
    List<Header> headers = Lists.newArrayList();
    while (headerNames.hasMoreElements()) {
      String headerName = (String) headerNames.nextElement();
      if (REQUEST_HEADER_WHITELIST.contains(headerName)) {
        String headerValue = request.getHeader(headerName);
        headers.add(new BasicHeader(headerName, headerValue));
      }
    }
    return headers.toArray(new Header[headers.size()]);
  }

  public static NameValuePair[] getRequestParameters(HttpServletRequest request, String... paramNames){
    Preconditions.checkNotNull(paramNames);
    List<NameValuePair> paramList = new ArrayList<>();
    Enumeration allParamNames = request.getParameterNames();
    while (allParamNames.hasMoreElements()) {
      String paramName = (String) allParamNames.nextElement();
      if (paramNames.length == 0 || Arrays.asList(paramNames).contains(paramName)) {
        paramList.add(new BasicNameValuePair(paramName, request.getParameter(paramName)));
      }
    }
    return paramList.toArray(new BasicNameValuePair[paramList.size()]);
  }


  public static HttpUriRequest buildRequest(URI fullUrl, String method, Header[] headers, NameValuePair[] params, NameValuePair... additionalParams) {
    RequestBuilder reqBuilder = RequestBuilder.create(method).setUri(fullUrl);
    if (headers != null) {for (Header header : headers) reqBuilder.addHeader(header);}
    if (params != null) {reqBuilder.addParameters(params);}
    if (additionalParams != null) {for (NameValuePair param: additionalParams) reqBuilder.addParameter(param);}
    return reqBuilder.build();
  }


}
