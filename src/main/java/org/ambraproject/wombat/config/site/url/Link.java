package org.ambraproject.wombat.config.site.url;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.ambraproject.wombat.config.site.HandlerDirectory;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.util.UrlParamBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

public class Link {

  private final Site site;
  private final String path;
  private final boolean isAbsolute;

  public Link(Site site, String path, boolean isAbsolute) {
    this.site = Preconditions.checkNotNull(site);
    this.path = Preconditions.checkNotNull(path);
    this.isAbsolute = isAbsolute;
  }


  public static Factory toLocalSite(Site localSite) {
    return new Factory(localSite, false);
  }

  public static Factory toForeignSite(Site localSite, Site foreignSite) {
    boolean isAbsolute = !localSite.getRequestScheme().getHostName().isPresent()
        && !foreignSite.getRequestScheme().getHostName().isPresent();
    return new Factory(foreignSite, isAbsolute);
  }

  public static Factory toForeignSite(Site localSite, String foreignJournalKey, SiteSet siteSet) {
    Site foreignSite;
    try {
      foreignSite = localSite.getTheme().resolveForeignJournalKey(siteSet, foreignJournalKey);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return toForeignSite(localSite, foreignSite);
  }

  public static class Factory {
    private final Site site;
    private final boolean isAbsolute;

    private Factory(Site site, boolean isAbsolute) {
      this.site = Preconditions.checkNotNull(site);
      this.isAbsolute = isAbsolute;
    }

    public Link toPath(String path) {
      return new Link(site, path, isAbsolute);
    }

    public Link toPattern(HandlerDirectory handlerDirectory, String handlerName, Map<String, ?> params) {
      String pattern = handlerDirectory.getPattern(handlerName, site);
      if (pattern == null) {
        String message = String.format("No handler with name=\"%s\" exists for site: %s", handlerName, site.getKey());
        throw new IllegalArgumentException(message);
      }
      return toPath(buildPathFromPattern(pattern, site, params));
    }
  }

  private static final String PATH_TEMPLATE_VAR = "path";
  private static final String SITE_TEMPLATE_VAR = "site";

  private static String buildPathFromPattern(String pattern, final Site site, final Map<String, ?> params) {
    // replace * or ** with the path URI template var to allow expansion when using ANT-style wildcards
    // TODO: support multiple wildcards using {path__0}, {path__1}?
    pattern = pattern.replaceFirst("\\*+", "{" + PATH_TEMPLATE_VAR + "}");

    UriComponentsBuilder builder = ServletUriComponentsBuilder.fromPath(pattern);
    UriComponents.UriTemplateVariables uriVariables = new UriComponents.UriTemplateVariables() {
      @Override
      public Object getValue(String name) {
        if (!params.containsKey(name)) {
          if (name.equals(SITE_TEMPLATE_VAR)) {
            return site.getKey();
          } else {
            throw new RuntimeException("Missing required parameter " + name);
          }
        }
        return params.remove(name); // consume params as they are used
      }
    };

    String path = builder.build().expand(uriVariables).encode().toString();

    if (!params.isEmpty()) {
      // any parameters not consumed by the path variable assignment process are assumed to be query params
      UrlParamBuilder paramBuilder = UrlParamBuilder.params();
      for (Map.Entry<String, ?> paramEntry : params.entrySet()) {
        paramBuilder.add(paramEntry.getKey(), paramEntry.getValue().toString());
      }
      path = path + "?" + paramBuilder.format();
    }

    return path;
  }


  public String get(HttpServletRequest request) {
    StringBuilder sb = new StringBuilder();
    if (isAbsolute) {
      appendPrefix(sb, request);
    }
    sb.append(request.getContextPath()).append('/');

    Optional<String> pathToken = site.getRequestScheme().getPathToken();
    if (pathToken.isPresent()) {
      sb.append(pathToken.get()).append('/');
    }

    sb.append(path);
    return sb.toString();
  }

  private static int getDefaultPort(HttpServletRequest request) {
    return request.isSecure() ? 443 : 80;
  }

  private void appendPrefix(StringBuilder sb, HttpServletRequest request) {
    sb.append(request.getScheme()).append("://");

    Optional<String> hostName = site.getRequestScheme().getHostName();
    sb.append(hostName.or(request.getServerName()));

    int serverPort = request.getServerPort();
    if (serverPort != getDefaultPort(request)) {
      sb.append(':').append(serverPort);
    }
  }


  @Override
  public String toString() {
    return "Link{" +
        "site=" + site +
        ", path='" + path + '\'' +
        ", isAbsolute=" + isAbsolute +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Link link = (Link) o;

    if (isAbsolute != link.isAbsolute) return false;
    if (!path.equals(link.path)) return false;
    if (!site.equals(link.site)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = site.hashCode();
    result = 31 * result + path.hashCode();
    result = 31 * result + (isAbsolute ? 1 : 0);
    return result;
  }
}
