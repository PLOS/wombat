package org.ambraproject.wombat.config.site;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestCondition;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * One instance of this class encapsulates, for one request mapping, the pattern conditions for all applicable sites. It
 * also represents which sites disable the request mapping, by not having a pattern condition for those sites.
 */
public class SiteRequestCondition implements RequestCondition<SiteRequestCondition> {

  private final SiteResolver siteResolver;
  private final ImmutableMap<Optional<Site>, PatternsRequestCondition> requestConditionMap;

  private SiteRequestCondition(SiteResolver siteResolver, Map<Optional<Site>, PatternsRequestCondition> requestConditionMap) {
    this.siteResolver = Preconditions.checkNotNull(siteResolver);
    this.requestConditionMap = ImmutableMap.copyOf(requestConditionMap);
  }

  /**
   * Get the set of patterns mapped for a request handler across all sites.
   *
   * @param siteSet     the set of all sites
   * @param baseMapping the unmodified annotation on the request handler
   * @return all patterns that are mapped to the request handler for any site in the set
   */
  public static Set<String> getAllPatterns(SiteSet siteSet, RequestMappingContext baseMapping) {
    if (baseMapping.isSiteless()) {
      return ImmutableSet.of(baseMapping.getPattern());
    }
    Set<RequestMappingContext> mappings = buildPatternMap(siteSet, baseMapping).keySet();
    ImmutableSet.Builder<String> patterns = ImmutableSet.builder();
    for (RequestMappingContext mapping : mappings) {
      patterns.add(mapping.getPattern());
    }
    return patterns.build();
  }

  /**
   * Create a condition, representing all sites, for a single request handler.
   * <p/>
   * Writes to the {@link RequestMappingContextDictionary} object as a side effect. To avoid redundant writes, this
   * method must be called only once per {@link RequestMapping} object.
   *
   * @param siteResolver                    the global site resolver
   * @param siteSet                         the set of all sites in the system
   * @param controllerMethod                the method annotated with the request handler
   * @param requestMappingContextDictionary the global handler directory, which must be in a writable state
   * @return the new condition object
   */
  public static SiteRequestCondition create(SiteResolver siteResolver, SiteSet siteSet,
                                            Method controllerMethod,
                                            RequestMappingContextDictionary requestMappingContextDictionary) {
    RequestMappingContext baseMapping = RequestMappingContext.create(controllerMethod);
    if (baseMapping.isSiteless()) {
      PatternsRequestCondition patternsRequestCondition = new PatternsRequestCondition(baseMapping.getPattern());
      requestMappingContextDictionary.registerGlobalMapping(baseMapping);
      ImmutableMap<Optional<Site>, PatternsRequestCondition> map = ImmutableMap.of(Optional.<Site>absent(), patternsRequestCondition);
      return new SiteRequestCondition(siteResolver, map);
    }

    Multimap<RequestMappingContext, Site> patternMap = buildPatternMap(siteSet, baseMapping);

    ImmutableMap.Builder<Optional<Site>, PatternsRequestCondition> requestConditionMap = ImmutableMap.builder();
    for (Map.Entry<RequestMappingContext, Collection<Site>> entry : patternMap.asMap().entrySet()) {
      RequestMappingContext mapping = entry.getKey();
      Collection<Site> sites = entry.getValue(); // all sites that share the mapping pattern

      PatternsRequestCondition condition = new PatternsRequestCondition(mapping.getPattern());
      for (Site site : sites) {
        requestConditionMap.put(Optional.of(site), condition);
        if (!mapping.getAnnotation().name().isEmpty()) {
          requestMappingContextDictionary.registerSiteMapping(mapping, site);
        }
      }
    }

    return new SiteRequestCondition(siteResolver, requestConditionMap.build());
  }

  /**
   * Construct a map from each pattern to the sites that use that pattern.
   */
  private static Multimap<RequestMappingContext, Site> buildPatternMap(SiteSet siteSet, RequestMappingContext baseMapping) {
    Preconditions.checkArgument(!baseMapping.isSiteless());
    Multimap<RequestMappingContext, Site> patterns = LinkedListMultimap.create();
    for (Site site : siteSet.getSites()) {
      RequestMappingContext mapping = getMappingForSite(baseMapping, site);
      if (mapping != null) {
        patterns.put(mapping, site);
      }
    }
    return patterns;
  }

  /**
   * Get the pattern that is mapped to a request handler for a given site. Return {@code null} if the handler is
   * disabled on that site.
   * <p/>
   * Looks up the configured value from the site's theme, or gets the default value from the mapping annotation if it is
   * not configured in the theme.
   *
   * @param mapping the request handler's mapping
   * @param site    the site
   * @return the pattern mapped to that handler on that site, or {@code null} if the handler is disabled on the site
   */
  private static RequestMappingContext getMappingForSite(RequestMappingContext mapping, Site site) {
    final Map<String, Object> mappingsConfig;
    try {
      mappingsConfig = site.getTheme().getConfigMap("mappings");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    Map<String, Object> override = (Map<String, Object>) mappingsConfig.get(mapping.getAnnotation().name());
    if (override != null) {
      Boolean disabled = (Boolean) override.get("disabled");
      if (disabled != null && disabled) {
        return null; // disabled == null means false by default
      }
      String overridePattern = (String) override.get("pattern");
      if (overridePattern != null) {
        mapping = mapping.override(overridePattern);
      }
    }

    if (site.getRequestScheme().hasPathToken()) {
      mapping = mapping.addSiteToken();
    }

    return mapping;
  }

  @Override
  public SiteRequestCondition getMatchingCondition(HttpServletRequest request) {
    Optional<Site> site = Optional.fromNullable(siteResolver.resolveSite(request));
    PatternsRequestCondition patternCondition = requestConditionMap.get(site);
    if (patternCondition == null) return null; // mapped handler is invalid for the site
    if (patternCondition.getMatchingCondition(request) == null) return null; // the URL is invalid for the site

    return requestConditionMap.size() == 1 ? this
        : new SiteRequestCondition(siteResolver, ImmutableMap.of(site, patternCondition));
  }

  /**
   *  This method should only be called when combining the typical method-level {@code RequestMapping} annotations with
   * those at the class-level. Since class-level {@code RequestMapping} annotations are not supported under the custom
   * {@code SiteHandlerMapping} handling, throw an exception. See {@link SiteHandlerMapping#checkMappingsOnHandlerType}
   * for details.
   * @param that another {@code SiteRequestCondition} instance to combine with
   * @return
   */
  @Override
  public SiteRequestCondition combine(SiteRequestCondition that) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int compareTo(SiteRequestCondition that, HttpServletRequest request) {
    Site site = siteResolver.resolveSite(request);
    RequestCondition thisCondition = this.requestConditionMap.get(site);
    RequestCondition thatCondition = that.requestConditionMap.get(site);

    return thisCondition == null && thatCondition == null ? 0
        : thisCondition == null ? 1
        : thatCondition == null ? -1
        : thisCondition.compareTo(thatCondition, request);
  }
}
