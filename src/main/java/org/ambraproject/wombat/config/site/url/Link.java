package org.ambraproject.wombat.config.site.url;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import org.ambraproject.wombat.config.site.HandlerDirectory;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.util.UrlParamBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Link {

  private final Site site;
  private final String path;
  private final boolean isAbsolute;

  private Link(Site site, String path, boolean isAbsolute) {
    this.site = Preconditions.checkNotNull(site);
    this.path = Preconditions.checkNotNull(path);
    this.isAbsolute = isAbsolute;
  }


  public static Factory toLocalSite(Site localSite) {
    return new Factory(localSite, false);
  }

  public static Factory toForeignSite(Site localSite, Site foreignSite) {
    Optional<String> localHostname = localSite.getRequestScheme().getHostName();
    Optional<String> foreignHostname = foreignSite.getRequestScheme().getHostName();
    final boolean isAbsolute;
    if (foreignHostname.isPresent()) {
      isAbsolute = localHostname.equals(foreignHostname);
    } else if (!localHostname.isPresent()) {
      isAbsolute = false;
    } else {
      throw new RuntimeException(String.format(""
              + "Cannot link to a site with no configured hostname (%s) from a site with one (%s; hostname=%s). "
              + "(Note: This error can be prevented by configuring a hostname either on every site or none.)",
          foreignSite.getKey(), localSite.getKey(), localHostname.get()));
    }
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

    public Link toPattern(HandlerDirectory handlerDirectory, String handlerName,
                          Map<String, ?> variables, Multimap<String, ?> queryParameters, List<?> wildcardValues) {
      String pattern = handlerDirectory.getPattern(handlerName, site);
      if (pattern == null) {
        String message = String.format("No handler with name=\"%s\" exists for site: %s", handlerName, site.getKey());
        throw new IllegalArgumentException(message);
      }
      return toPath(buildPathFromPattern(pattern, site, variables, queryParameters, wildcardValues));
    }
  }

  // Match path wildcards of one or two asterisks
  private static final Pattern WILDCARD = Pattern.compile("\\*\\*?");

  private static String buildPathFromPattern(String pattern, Site site,
                                             Map<String, ?> variables,
                                             Multimap<String, ?> queryParameters,
                                             List<?> wildcardValues) {
    Preconditions.checkNotNull(site);
    Preconditions.checkNotNull(variables);
    Preconditions.checkNotNull(queryParameters);

    if (site.getRequestScheme().hasPathToken()) {
      if (pattern.equals("/*") || pattern.startsWith("/*/")) {
        pattern = pattern.substring(2);
      } else {
        throw new RuntimeException("Pattern is inconsistent with site's request scheme");
      }
    }

    String path = fillVariables(pattern, variables);
    path = fillWildcardValues(path, wildcardValues);
    path = appendQueryParameters(queryParameters, path);

    return path;
  }

  private static String fillVariables(String path, final Map<String, ?> variables) {
    UriComponentsBuilder builder = ServletUriComponentsBuilder.fromPath(path);
    UriComponents.UriTemplateVariables uriVariables = new UriComponents.UriTemplateVariables() {
      @Override
      public Object getValue(String name) {
        Object value = variables.get(name);
        if (value == null) {
          throw new IllegalArgumentException("Missing required parameter " + name);
        }
        return value;
      }
    };

    return builder.build().expand(uriVariables).encode().toString();
  }

  private static String fillWildcardValues(String path, List<?> wildcardValues) {
    Matcher matcher = WILDCARD.matcher(path);
    boolean hasAtLeastOneWildcard = matcher.find();
    if (!hasAtLeastOneWildcard) {
      if (wildcardValues.isEmpty()) {
        return path;
      } else {
        throw complainAboutNumberOfWildcards(path, wildcardValues);
      }
    }

    StringBuffer filled = new StringBuffer(path.length() * 2);
    Iterator<?> valueIterator = wildcardValues.iterator();
    do {
      if (!valueIterator.hasNext()) throw complainAboutNumberOfWildcards(path, wildcardValues);
      String wildcardValue = valueIterator.next().toString();
      if (!matcher.group().equals("**") && wildcardValue.contains("/")) {
        String message = String.format("Cannot fill a multi-token value into a single-token wildcard. Value: \"%s\" Path: \"%s\"",
            wildcardValue, path);
        throw new IllegalArgumentException(message);
      }
      matcher.appendReplacement(filled, wildcardValue);
    } while (matcher.find());
    if (valueIterator.hasNext()) throw complainAboutNumberOfWildcards(path, wildcardValues);
    matcher.appendTail(filled);
    return filled.toString();
  }

  private static IllegalArgumentException complainAboutNumberOfWildcards(String path, List<?> wildcardValues) {
    int wildcardCount = 0;
    Matcher matcher = WILDCARD.matcher(path);
    while (matcher.find()) {
      wildcardCount++;
    }

    String message = String.format("Path has %d wildcard%s but was supplied %d value%s. Path=\"%s\" Values=%s",
        wildcardCount, (wildcardCount == 1 ? "" : "s"),
        wildcardValues.size(), (wildcardValues.size() == 1 ? "" : "s"),
        path, wildcardValues.toString());
    return new IllegalArgumentException(message);
  }

  private static String appendQueryParameters(Multimap<String, ?> queryParameters, String path) {
    if (!queryParameters.isEmpty()) {
      UrlParamBuilder paramBuilder = UrlParamBuilder.params();
      for (Map.Entry<String, ?> paramEntry : queryParameters.entries()) {
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
      sb.append(pathToken.get());
      if (!path.startsWith("/")) {
        sb.append('/');
      }
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
