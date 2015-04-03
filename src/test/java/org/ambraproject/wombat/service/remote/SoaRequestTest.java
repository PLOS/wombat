package org.ambraproject.wombat.service.remote;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

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

  @DataProvider
  public Object[][] goodSoaRequests() {
    return new Object[][]{
        // Simple cases
        {"foo", new String[]{}, new String[][]{}, "foo"},
        {"foo/bar", new String[]{}, new String[][]{}, "foo/bar"},
        {"foo", new String[]{"bar"}, new String[][]{}, "foo/bar"},
        {"foo", new String[]{"bar", "baz"}, new String[][]{}, "foo/bar/baz"},
        {"foo/bar", new String[]{"baz"}, new String[][]{}, "foo/bar/baz"},
        {"foo", new String[]{}, new String[][]{{"bar"}}, "foo?bar"},
        {"foo", new String[]{}, new String[][]{{"bar", "baz"}, {"qux"}}, "foo?bar=baz&qux"},
        {"foo", new String[]{}, new String[][]{{"bar", "baz"}, {"qux", "quux"}}, "foo?bar=baz&qux=quux"},
        {"foo", new String[]{}, new String[][]{{"bar", "baz"}, {"corge"}, {"qux", "quux"}}, "foo?bar=baz&corge&qux=quux"},
        {"foo/bar", new String[]{"baz"}, new String[][]{{"qux", "quux"}}, "foo/bar/baz?qux=quux"},

        // With escaping
        {"foo", new String[]{"&"}, new String[][]{}, "foo/%26"},
        {"foo", new String[]{}, new String[][]{{"?", "&"}}, "foo?%3F=%26"},
        {"foo", new String[]{}, new String[][]{{"bar=baz"}}, "foo?bar%3Dbaz"},
        {"foo", new String[]{}, new String[][]{{"?=&"}}, "foo?%3F%3D%26"},
        {"foo", new String[]{}, new String[][]{{"a?b", "c&d?e"}}, "foo?a%3Fb=c%26d%3Fe"},
        {"foo", new String[]{"<>"}, new String[][]{{"<foo bar=\"baz\">", "('!=')"}}, "foo/%3C%3E?%3Cfoo+bar%3D%22baz%22%3E=%28%27%21%3D%27%29"},
    };
  }

  @DataProvider
  public Object[][] badSoaRequests() {
    return new Object[][]{
        {"?", new String[]{}, new String[][]{}},
        {"foo", new String[]{"/"}, new String[][]{}},
        {"foo", new String[]{"?"}, new String[][]{}},
        {null, new String[]{}, new String[][]{}},
        {"foo", new String[]{null}, new String[][]{}},
        {"foo", new String[]{}, new String[][]{{null}}},
    };
  }

  private static SoaRequest applyTestInput(String path, String[] pathTokens, String[][] parameters) {
    SoaRequest.Builder builder = SoaRequest.request(path);
    for (String pathToken : pathTokens) {
      builder.addPathToken(pathToken);
    }
    for (String[] parameter : parameters) {
      switch (parameter.length) {
        case 1:
          builder.addParameter(parameter[0]);
          break;
        case 2:
          builder.addParameter(parameter[0], parameter[1]);
          break;
        default:
          fail("dataProvider error: Parameters must have 1 or 2 components");
      }
    }
    return builder.build();
  }

  @Test(dataProvider = "goodSoaRequests")
  public void testSoaRequest(String path, String[] pathTokens, String[][] parameters, String expected) {
    SoaRequest request = applyTestInput(path, pathTokens, parameters);
    assertEquals(request.toString(), expected);
    assertEquals(request.buildUri(STUB_SOA_SERVICE).toString(), SERVER_URL + expected);
  }

  @Test(dataProvider = "badSoaRequests", expectedExceptions = {IllegalArgumentException.class, NullPointerException.class})
  public void testSoaRequestErrors(String path, String[] pathTokens, String[][] parameters) {
    applyTestInput(path, pathTokens, parameters);
  }

}
