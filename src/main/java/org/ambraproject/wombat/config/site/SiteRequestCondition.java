package org.ambraproject.wombat.config.site;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestCondition;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.ambraproject.wombat.config.site.SiteScope.JOURNAL_NEUTRAL;
import static org.ambraproject.wombat.config.site.SiteScope.JOURNAL_SPECIFIC;

/**
 * One instance of this class encapsulates, for one request mapping, the pattern conditions for all applicable sites. It
 * also represents which sites disable the request mapping, by not having a pattern condition for those sites.
 */
public abstract class SiteRequestCondition implements RequestCondition<SiteRequestCondition> {

  private SiteRequestCondition() {
    // Nested implementations only
  }

  /**
   * Resolve a request to an appropriate PatternsRequestCondition. If needed, matches the request to the particular
   * PatternsRequestCondition that is appropriate for the {@link Site} that the request is hitting.
   *
   * @param request a request
   * @return the matched condition, or {@code null} if there is no condition for the site that the request is hitting
   */
  protected abstract PatternsRequestCondition resolve(HttpServletRequest request);

  /**
   * If this object covers more than one site, return a new condition that covers only the site that the request is
   * hitting. Motivated by {@link RequestCondition#getMatchingCondition}.
   *
   * @param request a request
   * @return a condition that covers only the site that the request is hitting
   */
  protected SiteRequestCondition narrow(HttpServletRequest request) {
    return this;
  }

  /**
   * Always returns the same pattern. Ignores the request, since we don't care about resolving to a site. In fact, the
   * request probably doesn't resolve to a site at all but is still valid.
   */
  private static SiteRequestCondition forSiteless(PatternsRequestCondition globalPattern) {
    Objects.requireNonNull(globalPattern);
    return new SiteRequestCondition() {
      @Override
      protected PatternsRequestCondition resolve(HttpServletRequest request) {
        return globalPattern;
      }
    };
  }

  /**
   * Returns a single pattern for requests that resolve to one site, or {@code null} otherwise.
   */
  private static SiteRequestCondition forSingleSite(SiteResolver siteResolver,
                                                    Site site, PatternsRequestCondition sitePattern) {
    Objects.requireNonNull(siteResolver);
    Objects.requireNonNull(site);
    Objects.requireNonNull(sitePattern);
    return new SiteRequestCondition() {
      @Override
      protected PatternsRequestCondition resolve(HttpServletRequest request) {
        return site.equals(siteResolver.resolveSite(request)) ? sitePattern : null;
      }
    };
  }

  /**
   * Contains the patterns for all sites that define an appropriate handler.
   */
  private static SiteRequestCondition forSiteMap(SiteResolver siteResolver,
                                                 ImmutableMap<Site, PatternsRequestCondition> requestConditionMap) {
    Objects.requireNonNull(siteResolver);
    Objects.requireNonNull(requestConditionMap);
    return new SiteRequestCondition() {
      @Override
      protected PatternsRequestCondition resolve(HttpServletRequest request) {
        return requestConditionMap.get(siteResolver.resolveSite(request));
      }

      @Override
      protected SiteRequestCondition narrow(HttpServletRequest request) {
        Site site = siteResolver.resolveSite(request);
        PatternsRequestCondition condition = requestConditionMap.get(site);
        return forSingleSite(siteResolver, site, condition);
      }
    };
  }


  /**
   * Get the set of patterns mapped for a request handler across all sites.
   *
   * @param siteSet     the set of all sites
   * @param baseMapping the unmodified annotation on the request handler
   * @return all patterns that are mapped to the request handler for any site in the set
   */
  public static Set<String> getAllPatterns(SiteSet siteSet, RequestMappingContext baseMapping) {
    if (baseMapping.hasScope(SiteScope.SITELESS)) {
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
   * <p>
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
    if (baseMapping.hasScope(SiteScope.SITELESS)) {
      PatternsRequestCondition patternsRequestCondition = new PatternsRequestCondition(baseMapping.getPattern());
      requestMappingContextDictionary.registerGlobalMapping(baseMapping);
      return forSiteless(patternsRequestCondition);
    }

    Multimap<RequestMappingContext, Site> patternMap = buildPatternMap(siteSet, baseMapping);

    ImmutableMap.Builder<Site, PatternsRequestCondition> requestConditionMap = ImmutableMap.builder();
    for (Map.Entry<RequestMappingContext, Collection<Site>> entry : patternMap.asMap().entrySet()) {
      RequestMappingContext mapping = entry.getKey();
      Collection<Site> sites = entry.getValue(); // all sites that share the mapping pattern

      PatternsRequestCondition condition = new PatternsRequestCondition(mapping.getPattern());
      for (Site site : sites) {
        requestConditionMap.put(site, condition);
        if (!mapping.getAnnotation().name().isEmpty()) {
          requestMappingContextDictionary.registerSiteMapping(mapping, site);
        }
      }
    }

    return forSiteMap(siteResolver, requestConditionMap.build());
  }

  /**
   * Construct a map from each pattern to the sites that use that pattern.
   */
  private static Multimap<RequestMappingContext, Site> buildPatternMap(SiteSet siteSet, RequestMappingContext baseMapping) {
    Preconditions.checkArgument(!baseMapping.hasScope(SiteScope.SITELESS));
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
   * Get the pattern that is mapped to a request handler for a given site. Return {@code null} if the handler should not
   * be mapped to a pattern on that site. May return {@code null} because the handler is disabled on that site by
   * configuration, or if the site is for a specific journal and the handler is journal-neutral or vice versa.
   * <p>
   * Looks up the configured value from the site's theme, or gets the default value from the mapping annotation if it is
   * not configured in the theme.
   *
   * @param mapping the request handler's mapping
   * @param site    the site
   * @return the pattern mapped to that handler on that site, or {@code null} to map no patterns
   */
  private static RequestMappingContext getMappingForSite(RequestMappingContext mapping, Site site) {
    Map<String, Object> mappingsConfig = site.getTheme().getConfigMap("mappings");

    if (!mapping.hasScope((site instanceof JournalSite) ? JOURNAL_SPECIFIC : JOURNAL_NEUTRAL)) {
      return null;
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
    PatternsRequestCondition patternCondition = resolve(request);
    if (patternCondition == null) return null; // mapped handler is invalid for the site
    if (patternCondition.getMatchingCondition(request) == null) return null; // the URL is invalid for the site

    return narrow(request);
  }

  /**
   * This method should only be called when combining the typical method-level {@code RequestMapping} annotations with
   * those at the class-level. Since class-level {@code RequestMapping} annotations are not supported under the custom
   * {@code SiteHandlerMapping} handling, throw an exception. See {@link SiteHandlerMapping#checkMappingsOnHandlerType}
   * for details.
   *
   * @param that another {@code SiteRequestCondition} instance to combine with
   * @return
   */
  @Override
  public SiteRequestCondition combine(SiteRequestCondition that) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int compareTo(SiteRequestCondition that, HttpServletRequest request) {
    PatternsRequestCondition thisCondition = this.resolve(request);
    PatternsRequestCondition thatCondition = that.resolve(request);

    return thisCondition == null && thatCondition == null ? 0
        : thisCondition == null ? 1
        : thatCondition == null ? -1
        : thisCondition.compareTo(thatCondition, request);
  }
}
