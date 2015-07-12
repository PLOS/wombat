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
  private static final String KEYNAME_SITEOVERRIDES = "siteOverrides";

  private Map<String, Map<String, ?>> handlerMapping;

  public HandlerMappingConfiguration(Map<String, Map<String, ?>> handlerMapping) {
    this.handlerMapping = handlerMapping;
  }

  public ImmutableSet<String> getValidPatternsForSite(String siteKey, SiteSet siteSet, Method method) {

    Set<String> validSites = getValidSites(siteSet, method);
    if (!validSites.contains(siteKey)) {
      return ImmutableSet.of();
    }

    ImmutableSet<String> sharedPatterns = getSharedPatterns(method);
    if (!hasSiteOverrides(method)) {
      return sharedPatterns;
    }

    return ImmutableSet.copyOf(Sets.difference(
            Sets.union(sharedPatterns, getSiteOverride(method, siteKey, KEYNAME_PATTERNS)),
            getSiteOverride(method, siteKey, KEYNAME_EXCLUDEPATTERNS)));
  }

  private boolean hasSiteMapping(Method method) {
    String handlerName = getHandlerName(method);
    return handlerMapping.get(handlerName) != null && ((handlerMapping.get(handlerName).get(KEYNAME_SITES) != null ||
            handlerMapping.get(handlerName).get(KEYNAME_EXCLUDESITES) != null));
  }

  private boolean hasSiteOverrides(Method method) {
    String handlerName = getHandlerName(method);
    return handlerMapping.get(handlerName) != null &&
            handlerMapping.get(handlerName).get(KEYNAME_SITEOVERRIDES) != null;
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

  private ImmutableSet<String> getSiteOverride(Method method, String siteKey, String key) {
    String handlerName = getHandlerName(method);
    List<Map<String, List<String>>> siteOverrides =
            (List<Map<String, List<String>>>) handlerMapping.get(handlerName).get(KEYNAME_SITEOVERRIDES);

    Set<String> overrideValue = new HashSet<>();
    for (Map<String, List<String>> siteOverride : siteOverrides) {
      if (siteOverride.get(KEYNAME_SITES) == null) {
        throw new RuntimeConfigurationException(
                String.format("HandlerMappingConfiguration ERROR: Site override handler mappings for " +
                        "method \"%s\" must all include a \"%s\" key value", handlerName, KEYNAME_SITES));
      }
      if ((siteOverride.get(KEYNAME_SITES)).contains(siteKey)) {
        overrideValue.addAll(siteOverride.get(key));
      }
    }

    return ImmutableSet.copyOf(overrideValue);
  }


  private String getHandlerName(Method method) {
    // use name property on associated RequestMapping annotation if present, otherwise, use class#method as name
    RequestMapping methodAnnotation = AnnotationUtils.findAnnotation(method, RequestMapping.class);
    if (methodAnnotation !=null && !methodAnnotation.name().isEmpty()) {
      return methodAnnotation.name();
    } else {
      String fullClassName = method.getDeclaringClass().getName();
      String className = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
      return className + "#" + method.getName();
    }
  }

}