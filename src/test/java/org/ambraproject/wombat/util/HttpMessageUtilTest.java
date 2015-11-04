package org.ambraproject.wombat.util;

import com.google.common.collect.ImmutableList;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.ambraproject.wombat.util.HttpMessageUtil.HeaderFilter;
import static org.ambraproject.wombat.util.HttpMessageUtil.copyResponseWithHeaders;
import static org.testng.Assert.assertEquals;

public class HttpMessageUtilTest {

  @Test
  public void testCopyResponseWithHeaders() throws IOException {
    byte[] testContent = "Test content".getBytes();

    HttpResponse input = new BasicHttpResponse(null, HttpStatus.OK.value(), "");
    BasicHttpEntity outputEntity = new BasicHttpEntity();
    outputEntity.setContent(new ByteArrayInputStream(testContent));
    input.setEntity(outputEntity);
    input.setHeader("includeMe", "foo");
    input.setHeader("excludeMe", "bar");
    input.setHeader("alterMe", "toBeAltered");

    HeaderFilter headerFilter = header -> {
      String name = header.getName();
      if ("includeMe".equalsIgnoreCase(name)) return header.getValue();
      if ("alterMe".equalsIgnoreCase(name)) return "altered";
      return null;
    };

    MockHttpServletResponse output = new MockHttpServletResponse();

    copyResponseWithHeaders(input, output, headerFilter);

    assertEquals(output.getContentAsByteArray(), testContent);

    assertEquals(output.getHeaderNames().size(), 2);
    assertEquals(output.getHeaders("includeMe"), ImmutableList.of("foo"));
    assertEquals(output.getHeaders("alterMe"), ImmutableList.of("altered"));
    assertEquals(output.getHeaders("excludeMe"), ImmutableList.of());
  }

}
