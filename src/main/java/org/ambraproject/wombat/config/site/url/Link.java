package org.ambraproject.wombat.config.site.url;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import org.ambraproject.wombat.config.site.RequestMappingContext;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.util.ClientEndpoint;
import org.ambraproject.wombat.util.UrlParamBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A link to a site page.
 * <p>
 * An instance of this class encapsulates a path to the linked page and the site to which the linked page belongs. It
 * depends on an {@code HttpServletRequest} object in order to build an {@code href} value to appear in the
 * corresponding response.
 */
public class Link {

  private final Optional<Site> site; // absent if the path is for a siteless handler
  private final String path;
  private final boolean isAbsolute;

  private Link(Optional<Site> site, String path, boolean isAbsolute) {
    this.site = Preconditions.checkNotNull(site);
    this.path = Preconditions.checkNotNull(path);
    this.isAbsolute = isAbsolute;
  }


  /**
   * Begin building a link to a page within the same site.
   *
   * @param localSite the site for both the originating page and the link target
   */
  public static Factory toLocalSite(Site localSite) {
    return new Factory(Optional.of(localSite), false);
  }

  /**
   * Begin building a link to an absolute address.
   * <p>
   * This should be used only if the resulting link will appear in a context outside of a local site, such as in a
   * downloadable document file. If the link will appear on a site page served by this application, instead use {@link
   * #toLocalSite} or {@link #toForeignSite} with a correct {@code localSite} argument.
   *
   * @param targetSite the site of the link target
   */
  public static Factory toAbsoluteAddress(Site targetSite) {
    return new Factory(Optional.of(targetSite), true);
  }

  /**
   * Begin building a link to a page on another site.
   *
   * @param localSite   the site of the originating page
   * @param foreignSite the site of the link target
   */
  public static Factory toForeignSite(Site localSite, Site foreignSite) {
    Optional<String> localHostname = localSite.getRequestScheme().getHostName();
    Optional<String> foreignHostname = foreignSite.getRequestScheme().getHostName();
    final boolean isAbsolute;
    if (foreignHostname.isPresent()) {
      isAbsolute = !localHostname.equals(foreignHostname);
    } else if (!localHostname.isPresent()) {
      isAbsolute = false;
    } else {
      throw new RuntimeException(String.format(""
              + "Cannot link to a site with no configured hostname (%s) from a site with one (%s; hostname=%s). "
              + "(Note: This error can be prevented by configuring a hostname either on every site or none.)",
          foreignSite.getKey(), localSite.getKey(), localHostname.get()));
    }
    return new Factory(Optional.of(foreignSite), isAbsolute);
  }

  /**
   * Begin building a link to a siteless handler. The returned factory object will throw exceptions if {@link
   * Factory#toPattern} is called for a handler that is not siteless, and will silently link to any path with no site
   * token if {@link Factory#toPath} is called.
   *
   * @see org.ambraproject.wombat.config.site.Siteless
   */
  public static Factory toSitelessHandler() {
    return SITELESS_FACTORY;
  }

  private static final Factory SITELESS_FACTORY = new Factory(Optional.empty(), true);

  /**
   * Begin building a link to a page on another site.
   *
   * @param localSite         the site of the originating page
   * @param foreignJournalKey the journal key of the target site
   * @param siteSet           the global site set
   */
  public static Factory toForeignSite(Site localSite, String foreignJournalKey, SiteSet siteSet) {
    Site foreignSite = localSite.getTheme().resolveForeignJournalKey(siteSet, foreignJournalKey);
    return toForeignSite(localSite, foreignSite);
  }

  /**
   * An intermediate builder class.
   */
  public static class Factory {
    private final Optional<Site> site; // if absent, may link only to siteless handlers
    private final boolean isAbsolute;

    private Factory(Optional<Site> site, boolean isAbsolute) {
      this.site = Preconditions.checkNotNull(site);
      this.isAbsolute = isAbsolute;
    }

    /**
     * Build a link to a direct path.
     *
     * @param path the path to link to
     */
    public Link toPath(String path) {
      return new Link(site, path, isAbsolute);
    }

    /**
     * Build a link that will hit a specified request handler.
     *
     * @param requestMappingContextDictionary the global handler directory
     * @param handlerName                     the name of the target request handler
     * @param variables                       values to fill in to path variables in the request handler's pattern
     * @param queryParameters                 query parameters to add to the end of the URL
     * @param wildcardValues                  values to substitute for wildcards in the request handler's pattern
     */
    public Link toPattern(RequestMappingContextDictionary requestMappingContextDictionary, String handlerName,
                          Map<String, ?> variables, Multimap<String, ?> queryParameters, List<?> wildcardValues) {
      RequestMappingContext mapping = requestMappingContextDictionary.getPattern(handlerName, site.orElse(null));
      if (mapping == null) {
        String message = site.isPresent()
            ? String.format("No handler with name=\"%s\" exists for site: %s", handlerName, site.get().getKey())
            : String.format("No siteless handler with name=\"%s\" exists", handlerName);
        throw new IllegalArgumentException(message);
      }

      final Optional<Site> linkSite;
      if (mapping.isSiteless()) {
        linkSite = Optional.empty();
      } else if (site.isPresent()) {
        linkSite = site;
      } else {
        throw new IllegalStateException("Can link only to Siteless handlers with a 'toSitelessHandler' Factory");
      }

      String path = buildPathFromPattern(mapping.getPattern(), linkSite, variables, queryParameters, wildcardValues);

      return new Link(linkSite, path, isAbsolute);
    }

    public PatternBuilder toPattern(RequestMappingContextDictionary requestMappingContextDictionary, String handlerName) {
      return new PatternBuilder(requestMappingContextDictionary, handlerName);
    }

    public class PatternBuilder {
      private final RequestMappingContextDictionary requestMappingContextDictionary;
      private final String handlerName;

      private final Map<String, Object> pathVariables = new LinkedHashMap<>();
      private final Multimap<String, Object> queryParameters = LinkedListMultimap.create();
      private final List<Object> wildcardValues = new ArrayList<>();

      private PatternBuilder(RequestMappingContextDictionary requestMappingContextDictionary, String handlerName) {
        this.requestMappingContextDictionary = Objects.requireNonNull(requestMappingContextDictionary);
        this.handlerName = Objects.requireNonNull(handlerName);
      }

      public PatternBuilder addPathVariables(Map<String, ?> pathVariables) {
        this.pathVariables.putAll(pathVariables);
        return this;
      }

      public PatternBuilder addPathVariable(String key, Object value) {
        this.pathVariables.put(key, value);
        return this;
      }

      public PatternBuilder addQueryParameters(Map<String, ?> queryParameters) {
        queryParameters.forEach(this.queryParameters::put);
        return this;
      }

      public PatternBuilder addQueryParameters(Multimap<String, ?> queryParameters) {
        this.queryParameters.putAll(queryParameters);
        return this;
      }

      public PatternBuilder addQueryParameter(String key, Object value) {
        this.queryParameters.put(key, value);
        return this;
      }

      public PatternBuilder addWildcardValues(List<?> wildcardValues) {
        this.wildcardValues.addAll(wildcardValues);
        return this;
      }

      public PatternBuilder addWildcardValue(Object wildcardValue) {
        this.wildcardValues.add(wildcardValue);
        return this;
      }

      public Link build() {
        return toPattern(requestMappingContextDictionary, handlerName, pathVariables, queryParameters, wildcardValues);
      }
    }
  }

  // Match path wildcards of one or two asterisks
  private static final Pattern WILDCARD = Pattern.compile("\\*\\*?");

  private static String buildPathFromPattern(String pattern, Optional<Site> site,
                                             Map<String, ?> variables,
                                             Multimap<String, ?> queryParameters,
                                             List<?> wildcardValues) {
    Preconditions.checkNotNull(site);
    Preconditions.checkNotNull(variables);
    Preconditions.checkNotNull(queryParameters);

    if (site.isPresent() && site.get().getRequestScheme().hasPathToken()) {
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
    UriComponents.UriTemplateVariables uriVariables = (String name) -> {
      Object value = variables.get(name);
      if (value == null) {
        throw new IllegalArgumentException("Missing required parameter " + name);
      }
      return value;
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


  /**
   * Build a link from this object. The returned value may be either an absolute link (full URL) or a relative link
   * (path beginning with "/") depending on the sites used to set up this object.
   * <p>
   * The returned path is suitable as an {@code href} value to be used in the response to the {@code request} argument.
   * The argument value must resolve to the local site given to set up this object.
   *
   * @param request the originating request of the page from which to link
   * @return a page that links from the originating page to the target page
   */
  public String get(HttpServletRequest request) {
    return get(request, false);
  }

  /**
   * Build a Spring view string that which, if returned from a Spring {@code RequestMapping} method, will redirect to
   * the linked address.
   *
   * @param request the originating request of the page from which to link
   * @return a Spring redirect string
   */
  public RedirectView getRedirect(HttpServletRequest request) {
    return new RedirectView(get(request, !isAbsolute));
  }

  /**
   * @param request     an originating request
   * @param isInContext {@code true} if the output should be relative to the application context; {@code false} if the
   *                    output should contain the application context path
   * @return the linked address
   */
  private String get(HttpServletRequest request, boolean isInContext) {
    StringBuilder sb = new StringBuilder();
    if (isAbsolute) {
      appendPrefix(sb, request);
    }
    if (!isInContext) {
      sb.append(request.getContextPath());
    }
    sb.append('/');

    Optional<String> pathToken = site.flatMap(s -> s.getRequestScheme().getPathToken());
    if (pathToken.isPresent()) {
      sb.append(pathToken.get()).append('/');
    }

    String path = this.path.startsWith("/") ? this.path.substring(1) : this.path;
    sb.append(path);

    return sb.toString();
  }

  private void appendPrefix(StringBuilder sb, HttpServletRequest request) {
    sb.append(request.getScheme()).append("://");

    ClientEndpoint clientEndpoint = ClientEndpoint.get(request);

    Optional<String> targetHostname = site.flatMap(s -> s.getRequestScheme().getHostName());
    sb.append(targetHostname.orElse(clientEndpoint.getHostname()));

    clientEndpoint.getPort().ifPresent(serverPort -> {
      sb.append(':').append(serverPort);
    });
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
