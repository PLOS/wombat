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

package org.ambraproject.wombat.util;

import static org.ambraproject.wombat.util.HttpMessageUtil.copyResponseWithHeaders;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.google.common.collect.ImmutableList;

import org.ambraproject.wombat.util.HttpMessageUtil.HeaderFilter;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

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

    assertArrayEquals(testContent, output.getContentAsByteArray());

    assertEquals(2, output.getHeaderNames().size());
    assertEquals(ImmutableList.of("foo"), output.getHeaders("includeMe"));
    assertEquals(ImmutableList.of("altered"), output.getHeaders("alterMe"));
    assertEquals(ImmutableList.of(), output.getHeaders("excludeMe"));
  }

}
