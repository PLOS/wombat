package org.ambraproject.wombat.config.site;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.ambraproject.wombat.config.theme.Theme;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestCondition;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class SiteRequestCondition implements RequestCondition<SiteRequestCondition> {

  private final SiteResolver siteResolver;
  private final ImmutableMap<Site, PatternsRequestCondition> requestConditionMap;

  private SiteRequestCondition(SiteResolver siteResolver, Map<Site, PatternsRequestCondition> requestConditionMap) {
    this.siteResolver = Preconditions.checkNotNull(siteResolver);
    this.requestConditionMap = ImmutableMap.copyOf(requestConditionMap);
  }

  public static Set<String> getAllPatterns(SiteSet siteSet, RequestMapping mappingAnnotation) {
    return buildPatternMap(siteSet, mappingAnnotation).keySet();
  }

  public static SiteRequestCondition create(SiteResolver siteResolver, SiteSet siteSet, RequestMapping mappingAnnotation) {
    Multimap<String, Site> patternMap = buildPatternMap(siteSet, mappingAnnotation);

    ImmutableMap.Builder<Site, PatternsRequestCondition> requestConditionMap = ImmutableMap.builder();
    for (Map.Entry<String, Collection<Site>> entry : patternMap.asMap().entrySet()) {
      String pattern = entry.getKey();
      PatternsRequestCondition condition = new PatternsRequestCondition(new String[]{pattern},
          null, null, true, true, null);
      for (Site site : entry.getValue()) {
        requestConditionMap.put(site, condition);
      }
    }

    return new SiteRequestCondition(siteResolver, requestConditionMap.build());
  }

  private static Multimap<String, Site> buildPatternMap(SiteSet siteSet, RequestMapping mappingAnnotation) {
    Multimap<String, Site> patterns = LinkedListMultimap.create();
    for (Site site : siteSet.getSites()) {
      String pattern = getPatternForTheme(mappingAnnotation, site.getTheme());
      if (pattern != null) {
        pattern = checkSitePathToken(site, pattern);
        patterns.put(pattern, site);
      }
    }
    return patterns;
  }

  private static String getPatternForTheme(RequestMapping mappingAnnotation, Theme theme) {
    String[] annotationValue = mappingAnnotation.value();
    if (annotationValue.length == 0) return null;
    String annotationPattern = Iterables.getOnlyElement(Arrays.asList(annotationValue));

    Map<String, Object> mappingsConfig;
    try {
      mappingsConfig = theme.getConfigMap("mappings");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Map<String, Object> override = (Map<String, Object>) mappingsConfig.get(mappingAnnotation.name());

    if (override == null) {
      return annotationPattern;
    } else {
      Boolean enabled = (Boolean) override.get("enabled");
      if (enabled == null || enabled) {
        String overridePattern = (String) override.get("pattern");
        return (overridePattern != null) ? overridePattern : annotationPattern;
      } else {
        return null;
      }
    }
  }

  private static String checkSitePathToken(Site site, String pattern) {
    if (true) {
      // TEMPORARY while RequestMappings have not been modified yet
      // TODO: Remove '/{site}' or '/*' from beginning of all RequestMapping patterns, then delete this
      return pattern;
    }
    return site.getRequestScheme().hasPathToken()
        ? (pattern.startsWith("/") ? "/*" : "/*/") + pattern
        : pattern;
  }

  @Override
  public SiteRequestCondition getMatchingCondition(HttpServletRequest request) {
    Site site = siteResolver.resolveSite(request);
    return requestConditionMap.containsKey(site) ? this : null;
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
