package org.ambraproject.wombat.service.remote;

import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static org.testng.Assert.assertEquals;

public class SoaRequestTest {

  private static final String SERVER_URL = "http://example.com/";
  private static final SoaService STUB_SOA_SERVICE = new SoaServiceImpl() {
    @Override
    public URL getServerUrl() {
      try {
        return new URL(SERVER_URL);
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
  };

  private void assertRequestEquals(URI actual, String expected) {
    assertEquals(actual.toString(), expected);
  }

  @Test
  public void testSoaRequest() {
    // Simple cases
    assertRequestEquals(
        SoaRequest.request("foo")
            .build().buildUri(STUB_SOA_SERVICE),
        SERVER_URL + "foo");
    assertRequestEquals(
        SoaRequest.request("foo")
            .addParameter("bar")
            .build().buildUri(STUB_SOA_SERVICE),
        SERVER_URL + "foo?bar");
    assertRequestEquals(
        SoaRequest.request("foo")
            .addParameter("bar", "baz")
            .build().buildUri(STUB_SOA_SERVICE),
        SERVER_URL + "foo?bar=baz");
    assertRequestEquals(
        SoaRequest.request("foo")
            .addParameter("bar", "baz")
            .addParameter("qux")
            .build().buildUri(STUB_SOA_SERVICE),
        SERVER_URL + "foo?bar=baz&qux");
    assertRequestEquals(
        SoaRequest.request("foo")
            .addParameter("bar", "baz")
            .addParameter("qux",
                "quux")
            .build().buildUri(STUB_SOA_SERVICE),
        SERVER_URL + "foo?bar=baz&qux=quux");
    assertRequestEquals(
        SoaRequest.request("foo")
            .addParameter("bar", "baz")
            .addParameter("qux", "quux")
            .build().buildUri(STUB_SOA_SERVICE),
        SERVER_URL + "foo?bar=baz&qux=quux");
    assertRequestEquals(
        SoaRequest.request("foo")
            .addParameter("bar", "baz")
            .addParameter("corge")
            .addParameter("qux", "quux")
            .build().buildUri(STUB_SOA_SERVICE),
        SERVER_URL + "foo?bar=baz&corge&qux=quux");
    assertRequestEquals(
        SoaRequest.request("foo/bar")
            .addParameter("qux", "quux")
            .build().buildUri(STUB_SOA_SERVICE),
        SERVER_URL + "foo/bar?qux=quux");

    // With escaping
    assertRequestEquals(
        SoaRequest.request("foo")
            .addParameter("?", "&")
            .build().buildUri(STUB_SOA_SERVICE),
        SERVER_URL + "foo?%3F=%26");
    assertRequestEquals(
        SoaRequest.request("foo")
            .addParameter("a?b", "c&d?e")
            .build().buildUri(STUB_SOA_SERVICE),
        SERVER_URL + "foo?a%3Fb=c%26d%3Fe");
    assertRequestEquals(
        SoaRequest.request("foo")
            .addParameter("<foo bar=\"baz\">", "('!=')")
            .build().buildUri(STUB_SOA_SERVICE),
        SERVER_URL + "foo?%3Cfoo+bar%3D%22baz%22%3E=%28%27%21%3D%27%29");
  }

}
