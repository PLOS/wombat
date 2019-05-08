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

package org.ambraproject.wombat.config.site.url;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.theme.StubTheme;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.mock.web.MockHttpServletRequest;

@RunWith(Parameterized.class)
public class LinkTest {

  private static Site dummySite(String siteName, SiteRequestScheme scheme) {
    return new Site(siteName, new StubTheme(siteName, siteName), scheme, "");
  }

  private static final Site TOKEN_1 = dummySite("token1",
      SiteRequestScheme.builder().setPathToken("site1").build());
  private static final Site TOKEN_2 = dummySite("token2",
      SiteRequestScheme.builder().setPathToken("site2").build());
  private static final Site HOST_AND_TOKEN_1 = dummySite("hostAndToken1",
      SiteRequestScheme.builder().setPathToken("site3").specifyHost("1.example.com").build());
  private static final Site HOST_AND_TOKEN_2 = dummySite("hostAndToken2",
      SiteRequestScheme.builder().setPathToken("site4").specifyHost("2.example.com").build());
  private static final Site HOST_AND_TOKEN_3 = dummySite("hostAndToken3",
      SiteRequestScheme.builder().setPathToken("site5").specifyHost("2.example.com").build());
  private static final Site HOST_ONLY = dummySite("hostOnly",
      SiteRequestScheme.builder().specifyHost("hostOnly.example.com").build());

  @Parameters
  public static List<Object[]> toPathCases() {
    List<Object[]> cases = new ArrayList<>();

    // Cases where the HttpServletRequest has no interesting properties
    Object[][] simpleCases = new Object[][]{
        {Link.toLocalSite(TOKEN_1).toPath(""), "/site1/"},
        {Link.toLocalSite(TOKEN_1).toPath("path"), "/site1/path"},
        {Link.toLocalSite(TOKEN_1).toPath("/path"), "/site1/path"},
        {Link.toLocalSite(TOKEN_1).toPath("path/"), "/site1/path/"},
        {Link.toLocalSite(TOKEN_1).toPath("/path/"), "/site1/path/"},
        {Link.toForeignSite(TOKEN_1, TOKEN_2).toPath("path"), "/site2/path"},
        {Link.toForeignSite(TOKEN_1, TOKEN_1).toPath("path"), "/site1/path"},
        {Link.toForeignSite(TOKEN_1, HOST_AND_TOKEN_1).toPath("path"), "http://1.example.com/site3/path"},
        {Link.toForeignSite(HOST_AND_TOKEN_1, HOST_AND_TOKEN_2).toPath("path"), "http://2.example.com/site4/path"},
        {Link.toForeignSite(HOST_AND_TOKEN_2, HOST_AND_TOKEN_3).toPath("path"), "/site5/path"},
        {Link.toLocalSite(HOST_ONLY).toPath(""), "/"},
        {Link.toLocalSite(HOST_ONLY).toPath("path"), "/path"},
        {Link.toLocalSite(HOST_ONLY).toPath("/path"), "/path"},
        {Link.toAbsoluteAddress(HOST_ONLY).toPath("path"), "http://hostOnly.example.com/path"},
        {Link.toAbsoluteAddress(HOST_ONLY).toPath("/path"), "http://hostOnly.example.com/path"},
        {Link.toForeignSite(TOKEN_1, HOST_ONLY).toPath("path"), "http://hostOnly.example.com/path"},
        {Link.toForeignSite(TOKEN_1, HOST_ONLY).toPath("/path"), "http://hostOnly.example.com/path"},
    };
    MockHttpServletRequest simpleReq = new MockHttpServletRequest();
    for (Object[] simpleCase : simpleCases) {
      cases.add(new Object[]{(Link) simpleCase[0], simpleReq, (String) simpleCase[1]});
    }

    // Cases with special HttpServletRequest objects

    MockHttpServletRequest reqWithContextPath = new MockHttpServletRequest();
    reqWithContextPath.setContextPath("/context");
    cases.add(new Object[]{Link.toLocalSite(TOKEN_1).toPath("path"), reqWithContextPath, "/context/site1/path"});
    cases.add(new Object[]{Link.toForeignSite(TOKEN_1, HOST_AND_TOKEN_1).toPath("path"), reqWithContextPath, "http://1.example.com/context/site3/path"});

    MockHttpServletRequest reqWithPort = new MockHttpServletRequest();
    reqWithPort.setServerPort(8765);
    cases.add(new Object[]{Link.toLocalSite(TOKEN_1).toPath("path"), reqWithPort, "/site1/path"});
    cases.add(new Object[]{Link.toForeignSite(TOKEN_1, HOST_AND_TOKEN_1).toPath("path"), reqWithPort, "http://1.example.com:8765/site3/path"});
    cases.add(new Object[]{Link.toLocalSite(HOST_ONLY).toPath("path"), reqWithPort, "/path"});
    cases.add(new Object[]{Link.toForeignSite(TOKEN_1, HOST_ONLY).toPath("path"), reqWithPort, "http://hostOnly.example.com:8765/path"});

    MockHttpServletRequest reqSecure = new MockHttpServletRequest();
    reqSecure.setSecure(true);
    reqSecure.setScheme("https");
    reqSecure.setServerPort(443);
    cases.add(new Object[]{Link.toLocalSite(TOKEN_1).toPath("path"), reqSecure, "/site1/path"});
    cases.add(new Object[]{Link.toForeignSite(TOKEN_1, HOST_AND_TOKEN_1).toPath("path"), reqSecure, "http://1.example.com/site3/path"});
    cases.add(new Object[]{Link.toLocalSite(HOST_ONLY).toPath("path"), reqSecure, "/path"});
    cases.add(new Object[]{Link.toForeignSite(TOKEN_1, HOST_ONLY).toPath("path"), reqSecure, "http://hostOnly.example.com/path"});

    MockHttpServletRequest proxiedReq = new MockHttpServletRequest();
    proxiedReq.setServerName("internal-frontend.example.com");
    proxiedReq.setServerPort(8081);
    proxiedReq.addHeader("X-Forwarded-Host", "public-frontend.example.com");
    cases.add(new Object[]{Link.toAbsoluteAddress(TOKEN_1).toPath("path"), proxiedReq, "http://public-frontend.example.com/site1/path"});
    cases.add(new Object[]{Link.toLocalSite(TOKEN_1).toPath("path"), proxiedReq, "/site1/path"});
    cases.add(new Object[]{Link.toForeignSite(TOKEN_1, HOST_AND_TOKEN_1).toPath("path"), proxiedReq, "http://1.example.com/site3/path"});
    cases.add(new Object[]{Link.toLocalSite(HOST_ONLY).toPath("path"), proxiedReq, "/path"});
    cases.add(new Object[]{Link.toForeignSite(TOKEN_1, HOST_ONLY).toPath("path"), proxiedReq, "http://hostOnly.example.com/path"});

    MockHttpServletRequest proxiedPortReq = new MockHttpServletRequest();
    proxiedPortReq.setServerName("internal-frontend.example.com");
    proxiedPortReq.setServerPort(8081);
    proxiedPortReq.addHeader("X-Forwarded-Host", "public-frontend.example.com:8082");
    cases.add(new Object[]{Link.toAbsoluteAddress(TOKEN_1).toPath("path"), proxiedPortReq, "http://public-frontend.example.com:8082/site1/path"});
    cases.add(new Object[]{Link.toLocalSite(TOKEN_1).toPath("path"), proxiedPortReq, "/site1/path"});
    cases.add(new Object[]{Link.toForeignSite(TOKEN_1, HOST_AND_TOKEN_1).toPath("path"), proxiedPortReq, "http://1.example.com:8082/site3/path"});
    cases.add(new Object[]{Link.toLocalSite(HOST_ONLY).toPath("path"), proxiedPortReq, "/path"});
    cases.add(new Object[]{Link.toForeignSite(TOKEN_1, HOST_ONLY).toPath("path"), proxiedPortReq, "http://hostOnly.example.com:8082/path"});

    return cases;
  }

  @Parameter(0)
  public Link link;

  @Parameter(1)
  public MockHttpServletRequest request;

  @Parameter(2)
  public String expectedPath;

  public void testToPath() {
    assertEquals(expectedPath, link.get(request));
  }

  @Test(expected = RuntimeException.class)
  public void testInvalidToPathCase() {
    Link.toForeignSite(HOST_AND_TOKEN_1, TOKEN_1);
  }

}
