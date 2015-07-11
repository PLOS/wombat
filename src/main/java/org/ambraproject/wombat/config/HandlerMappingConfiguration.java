package org.ambraproject.wombat.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.ambraproject.wombat.config.site.SiteSet;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.*;


public class HandlerMappingConfiguration {

  private static final String KEYNAME_SITES = "sites";
  private static final String KEYNAME_EXCLUDESITES = "excludeSites";
  private static final String KEYNAME_PATTERNS = "patterns";
  private static final String KEYNAME_EXCLUDEPATTERNS = "excludePatterns";
  private static final String KEYNAME_SITEPATTERNS = "sitePatterns";

  private Map<String, Map<String, ?>> handlerMapping;

  public HandlerMappingConfiguration(Map<String, Map<String, ?>> handlerMapping) {
    this.handlerMapping = handlerMapping;
  }

  public ImmutableSet<String> getValidPatternsForSite(String siteKey, SiteSet siteSet, Method method) {

    String handlerName = getHandlerName(method);
    Set<String> validSites = getValidSites(siteSet, method);
    if (!validSites.contains(siteKey)) {
      return ImmutableSet.of();
    }

    ImmutableSet<String> sharedPatterns = getSharedPatterns(method);
    if (!hasSitePatternsMapping(method)) {
      return sharedPatterns;
    }

    // iterate through the site-pattern mappings and accumulate patterns for the given site
    List<Map<String, List<String>>> sitePatternMappings =
      (List<Map<String, List<String>>>) handlerMapping.get(handlerName).get(KEYNAME_SITEPATTERNS);

    Set<String> includedPatterns = new HashSet<>();
    Set<String> excludedPatterns = new HashSet<>();
    for (Map<String, List<String>> siteMapping : sitePatternMappings) {
      if (siteMapping.get(KEYNAME_SITES) == null) {
        throw new RuntimeConfigurationException(
                String.format("HandlerMappingConfiguration ERROR: Site-patterns handler mapping for " +
                                "method \"%s\" must include a \"%s\" key value", handlerName, KEYNAME_SITES));
      }
      if ((siteMapping.get(KEYNAME_SITES)).contains(siteKey)) {
        siteMapping.get(KEYNAME_PATTERNS)
      }
    }

  }

  private boolean hasSiteMapping(Method method) {
    String handlerName = getHandlerName(method);
    return handlerMapping.get(handlerName) != null && ((handlerMapping.get(handlerName).get(KEYNAME_SITES) != null ||
            handlerMapping.get(handlerName).get(KEYNAME_EXCLUDESITES) != null));
  }

  private boolean hasSitePatternsMapping(Method method) {
    String handlerName = getHandlerName(method);
    return handlerMapping.get(handlerName) != null &&
            (handlerMapping.get(handlerName).get(KEYNAME_SITEPATTERNS) != null;
  }

  private boolean hasPatternsMapping(Method method) {
    String handlerName = getHandlerName(method);
    return handlerMapping.get(handlerName) != null && ((handlerMapping.get(handlerName).get(KEYNAME_PATTERNS) != null ||
            handlerMapping.get(handlerName).get(KEYNAME_EXCLUDEPATTERNS) != null));
  }

  private ImmutableSet<String> getValidSites(SiteSet siteSet, Method method) {

    if (!hasSiteMapping(method)) {
      return siteSet.getSiteKeys();
    }

    return ImmutableSet.copyOf(Sets.difference(
            getMethodConfig(method, KEYNAME_SITES, siteSet.getSiteKeys()),
            getMethodConfig(method, KEYNAME_EXCLUDESITES)));
  }

  private ImmutableSet<String> getSharedPatterns(Method method) {
    // returns mapped patterns for the given method that are shared across all sites

    // retrieve default patterns defined in the RequestMapping annotation
    ImmutableSet<String> annotationDefinedPatterns;
    RequestMapping methodAnnotation = AnnotationUtils.findAnnotation(method, RequestMapping.class);
    if (methodAnnotation == null) {
      annotationDefinedPatterns = ImmutableSet.of();
    } else {
      annotationDefinedPatterns = ImmutableSet.copyOf(methodAnnotation.value());
    }

    if (!hasPatternsMapping(method)) {
      return annotationDefinedPatterns;
    }

    // override with patterns from this config
    return ImmutableSet.copyOf(Sets.difference(
                Sets.union(annotationDefinedPatterns, getMethodConfig(method, KEYNAME_PATTERNS)),
                getMethodConfig(method, KEYNAME_EXCLUDEPATTERNS)));

  }


  private ImmutableSet<String> getMethodConfig(Method method, String key) {
    return getMethodConfig(method, key, ImmutableSet.<String>of());
  }

  private ImmutableSet<String> getMethodConfig(Method method, String key, Object defaultVal) {
    String handlerName = getHandlerName(method);
    List<String> methodVal = (List<String>) handlerMapping.get(handlerName).get(key);
    return methodVal != null ? ImmutableSet.copyOf(methodVal) : ImmutableSet.copyOf((List<String>)defaultVal);
  }

  private ImmutableSet<String> getSitePatternConfig(Method method, String siteKey, String key) {
    String handlerName = getHandlerName(method);
    List<Map<String, List<String>>> sitePatternMappings =
            (List<Map<String, List<String>>>) handlerMapping.get(handlerName).get(KEYNAME_SITEPATTERNS);

    List<String> methodVal = (List<String>) handlerMapping.get(handlerName).get(KEYNAME_SITEPATTERNS);
    return methodVal != null ? ImmutableSet.copyOf(methodVal) : ImmutableSet.copyOf((List<String>)defaultVal);
  }


  private String getHandlerName(Method method) {
    String fullClassName = method.getDeclaringClass().getName();
    String className = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
    return className + "#" + method.getName();
  }

}
