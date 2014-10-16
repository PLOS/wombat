package org.ambraproject.wombat.util;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
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
import java.util.*;

/**
 * A utility class for creation and management of HTTP messages
 */
public class HttpMessageUtil {


  /**
   * Copy content with whitelisted headers between responses
   *
   * @param responseTo
   * @param headerWhitelist
   * @throws IOException
   */
  public static void copyResponseWithHeaders(CloseableHttpResponse responseFrom, HttpServletResponse responseTo,
                                             ImmutableSet<String> headerWhitelist)
          throws IOException {
    for (Header header : responseFrom.getAllHeaders()) {
      if (headerWhitelist.contains(header.getName())) {
        responseTo.setHeader(header.getName(), header.getValue());
      }
    }
    copyResponse(responseFrom, responseTo);
  }

  /**
   * Copy content between responses
   * @param responseFrom
   * @param responseTo
   * @throws IOException
   */
  public static void copyResponse(CloseableHttpResponse responseFrom, HttpServletResponse responseTo)
          throws IOException {

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
  public static Collection<Header> getRequestHeaders(HttpServletRequest request, ImmutableSet<String> headerWhitelist) {
    Enumeration headerNames = request.getHeaderNames();
    List<Header> headers = Lists.newArrayList();
    while (headerNames.hasMoreElements()) {
      String headerName = (String) headerNames.nextElement();
      if (headerWhitelist.contains(headerName)) {
        String headerValue = request.getHeader(headerName);
        headers.add(new BasicHeader(headerName, headerValue));
      }
    }
    return headers;
  }



  public static Collection<NameValuePair> getRequestParameters(HttpServletRequest request) {
    return getRequestParameters(request, ImmutableSet.<String>of());
  }

  public static Collection<NameValuePair> getRequestParameters(HttpServletRequest request, Set<String> paramNames) {
    Preconditions.checkNotNull(paramNames);
    List<NameValuePair> paramList = new ArrayList<>();
    Enumeration allParamNames = request.getParameterNames();
    while (allParamNames.hasMoreElements()) {
      String paramName = (String) allParamNames.nextElement();
      if (paramNames.isEmpty() || paramNames.contains(paramName)) {
        paramList.add(new BasicNameValuePair(paramName, request.getParameter(paramName)));
      }
    }
    return paramList;
  }


  public static HttpUriRequest buildRequest(URI fullUrl, String method) {
    return buildRequest(fullUrl, method, ImmutableSet.<Header>of(), ImmutableSet.<NameValuePair>of());
  }


  public static HttpUriRequest buildRequest(URI fullUrl, String method,
                                            Collection<? extends NameValuePair> params,
                                            NameValuePair... additionalParams) {
    return buildRequest(fullUrl, method, ImmutableSet.<Header>of(), params, additionalParams);
  }


  public static HttpUriRequest buildRequest(URI fullUrl, String method,
                                            Collection<? extends Header> headers,
                                            Collection<? extends NameValuePair> params,
                                            NameValuePair... additionalParams) {
    RequestBuilder reqBuilder = RequestBuilder.create(method).setUri(fullUrl);
    Preconditions.checkNotNull(headers);
    Preconditions.checkNotNull(params);
    Preconditions.checkNotNull(additionalParams);
    for (Header header : headers) {
      reqBuilder.addHeader(header);
    }
    if (!params.isEmpty()) {
      reqBuilder.addParameters(params.toArray(new NameValuePair[params.size()]));
    }
    for (NameValuePair param : additionalParams) {
      reqBuilder.addParameter(param);
    }
    return reqBuilder.build();
  }


}
