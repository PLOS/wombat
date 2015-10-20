package org.ambraproject.wombat.config.site;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
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
  private final ImmutableMap<Site, PatternsRequestCondition> requestConditionMap;

  private SiteRequestCondition(SiteResolver siteResolver, Map<Site, PatternsRequestCondition> requestConditionMap) {
    this.siteResolver = Preconditions.checkNotNull(siteResolver);
    this.requestConditionMap = ImmutableMap.copyOf(requestConditionMap);
  }

  /**
   * Get the set of patterns mapped for a request handler across all sites.
   *
   * @param siteSet           the set of all sites
   * @param mappingAnnotation the annotation representing to the request handler
   * @return all patterns that are mapped to the request handler for any site in the set
   */
  public static Set<String> getAllPatterns(SiteSet siteSet, RequestMapping mappingAnnotation) {
    return ImmutableSet.of(); // buildPatternMap(siteSet, mappingAnnotation).keySet();
  }

  /**
   * Create a condition, representing all sites, for a single request handler.
   * <p/>
   * Writes to the {@link RequestHandlerPatternDictionary} object as a side effect. To avoid redundant writes, this
   * method must be called only once per {@link RequestMapping} object.
   *
   * @param siteResolver                    the global site resolver
   * @param siteSet                         the set of all sites in the system
   * @param mappingAnnotation               the annotation representing the request handler
   * @param requestHandlerPatternDictionary the global handler directory, which must be in a writable state
   * @return the new condition object
   */
  public static SiteRequestCondition create(SiteResolver siteResolver, SiteSet siteSet,
                                            Method controllerMethod,
                                            RequestHandlerPatternDictionary requestHandlerPatternDictionary) {
    RequestMappingValue baseMapping = RequestMappingValue.create(controllerMethod);
    if (baseMapping.isSiteless()) {
      SiteRequestCondition sitelessCondition = null; // TODO: Implement
      // TODO: Represent in dictionary somehow
      return sitelessCondition;
    }

    Multimap<RequestMappingValue, Site> patternMap = buildPatternMap(siteSet, baseMapping);

    ImmutableMap.Builder<Site, PatternsRequestCondition> requestConditionMap = ImmutableMap.builder();
    for (Map.Entry<RequestMappingValue, Collection<Site>> entry : patternMap.asMap().entrySet()) {
      RequestMappingValue mapping = entry.getKey();
      PatternsRequestCondition condition = new PatternsRequestCondition(new String[]{mapping.getPattern()},
          null, null, true, true, null);
      for (Site site : entry.getValue()) {
        requestConditionMap.put(site, condition);
        if (!mapping.getAnnotation().name().isEmpty()) {
          requestHandlerPatternDictionary.register(mapping, site);
        }
      }
    }

    return new SiteRequestCondition(siteResolver, requestConditionMap.build());
  }

  /**
   * Construct a map from each pattern to the sites that use that pattern.
   */
  private static Multimap<RequestMappingValue, Site> buildPatternMap(SiteSet siteSet, RequestMappingValue baseMapping) {
    Multimap<RequestMappingValue, Site> patterns = LinkedListMultimap.create();
    for (Site site : siteSet.getSites()) {
      RequestMappingValue pattern = getPatternForSite(baseMapping, site);
      if (pattern != null) {
        patterns.put(pattern, site);
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
   * @param controllerMethod the annotation representing the request handler
   * @param site             the site
   * @return the pattern mapped to that handler on that site, or {@code null} if the handler is disabled on the site
   */
  private static RequestMappingValue getPatternForSite(RequestMappingValue mapping, Site site) {
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
    Site site = siteResolver.resolveSite(request);
    PatternsRequestCondition patternCondition = requestConditionMap.get(site);
    if (patternCondition == null) return null; // mapped handler is invalid for the site
    if (patternCondition.getMatchingCondition(request) == null) return null; // the URL is invalid for the site

    // TODO: Instead, return this?
    return requestConditionMap.size() == 1 ? this
        : new SiteRequestCondition(siteResolver, ImmutableMap.of(site, patternCondition));
  }

  @Override
  public SiteRequestCondition combine(SiteRequestCondition that) {
    if (this.requestConditionMap.equals(that.requestConditionMap)) return this;
    ImmutableMap.Builder<Site, PatternsRequestCondition> combinedMap = ImmutableMap.builder();

    Set<Site> patterns = Sets.union(this.requestConditionMap.keySet(), that.requestConditionMap.keySet());
    for (Site pattern : patterns) {
      PatternsRequestCondition thisCondition = this.requestConditionMap.get(pattern);
      PatternsRequestCondition thatCondition = that.requestConditionMap.get(pattern);
      PatternsRequestCondition combinedCondition = thisCondition == null ? thatCondition : thatCondition == null ? thisCondition :
          thisCondition.combine(thatCondition);
      combinedMap.put(pattern, combinedCondition);
    }

    return new SiteRequestCondition(siteResolver, combinedMap.build());
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
