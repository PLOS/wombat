package org.ambraproject.wombat.config;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.ambraproject.wombat.config.site.SiteSet;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HandlerMappingConfiguration {

  private static final String KEYNAME_SITES = "sites";
  private static final String KEYNAME_EXCLUDESITES = "excludeSites";
  private static final String KEYNAME_PATTERNS = "patterns";
  private static final String KEYNAME_INCLUDEPATTERNS = "includePatterns";
  private static final String KEYNAME_EXCLUDEPATTERNS = "excludePatterns";
  private static final String KEYNAME_REPLACEPATTERNS = "replacePatterns";
  private static final String KEYNAME_SITEOVERRIDES = "siteOverrides";
  public static final String VALUE_NULLSITE = "undefined";

  private final ImmutableSet<String> siteKeys;
  private final Map<String, Map<String, ?>> handlerMapping;
  // handlerMethods lookup is set by SiteMappingHandlerMapping during that bean's post processing
  public ImmutableMap<String, HandlerMethod> handlerMethods;

  public HandlerMappingConfiguration(Map<String, Map<String, ?>> handlerMapping, SiteSet siteSet) {
    this.handlerMapping = handlerMapping != null ? handlerMapping :
        ImmutableMap.<String, Map<String, ?>>of("", ImmutableMap.<String, Object>of());
    this.siteKeys = ImmutableSet.<String>builder()
        .addAll(siteSet.getSiteKeys())
        .add(VALUE_NULLSITE)
        .build();
  }

  public ImmutableSet<String> getValidPatternsForSite(String handlerName, String siteKey) {
    return getValidPatternsForSite(getRequestMapping(handlerName), siteKey);
  }

  public ImmutableSet<String> getValidPatternsForSite(RequestMapping handlerAnnotation, String siteKey) {
    if (!siteKeys.contains(siteKey)) {
      throw new RuntimeConfigurationException(String.format("HandlerMappingConfiguration ERROR: " +
          "Requested handler mapping configuration for non-existing site: \"%s\"", siteKey));
    }

    Set<String> validSites = getValidSites(handlerAnnotation);
    if (!validSites.contains(siteKey)) {
      return ImmutableSet.of();
    }

    ImmutableSet<String> sharedPatterns = getSharedPatterns(handlerAnnotation);
    if (!hasSiteOverrides(handlerAnnotation)) {
      return sharedPatterns;
    }

    ImmutableSet<String> replacedPatterns = getSiteOverride(handlerAnnotation, siteKey, KEYNAME_REPLACEPATTERNS);

    return !replacedPatterns.isEmpty() ? replacedPatterns : ImmutableSet.copyOf(Sets.difference(
        Sets.union(sharedPatterns, getSiteOverride(handlerAnnotation, siteKey, KEYNAME_INCLUDEPATTERNS)),
        getSiteOverride(handlerAnnotation, siteKey, KEYNAME_EXCLUDEPATTERNS)));
  }

  public boolean hasSiteMapping(RequestMapping handlerAnnotation) {
    String handlerName = getHandlerName(handlerAnnotation);
    return handlerMapping.get(handlerName) != null &&
        ((handlerMapping.get(handlerName).get(KEYNAME_SITES) != null ||
            handlerMapping.get(handlerName).get(KEYNAME_EXCLUDESITES) != null));
  }

  public boolean hasSiteOverrides(RequestMapping handlerAnnotation) {
    String handlerName = getHandlerName(handlerAnnotation);
    return handlerMapping.get(handlerName) != null &&
        handlerMapping.get(handlerName).get(KEYNAME_SITEOVERRIDES) != null;
  }

  private boolean hasPatternsMapping(RequestMapping handlerAnnotation) {
    String handlerName = getHandlerName(handlerAnnotation);
    return handlerMapping.get(handlerName) != null &&
        ((handlerMapping.get(handlerName).get(KEYNAME_PATTERNS) != null ||
            handlerMapping.get(handlerName).get(KEYNAME_EXCLUDEPATTERNS) != null));
  }

  public ImmutableSet<String> getValidSites(RequestMapping handlerAnnotation) {
    if (!hasSiteMapping(handlerAnnotation)) {
      return siteKeys;
    }

    return ImmutableSet.copyOf(Sets.difference(
        getMethodConfig(handlerAnnotation, KEYNAME_SITES, siteKeys),
        getMethodConfig(handlerAnnotation, KEYNAME_EXCLUDESITES)));
  }

  public ImmutableSet<String> getSharedPatterns(RequestMapping handlerAnnotation) {
    // returns mapped patterns for the given method that are shared across all sites

    // retrieve default patterns defined in the RequestMapping annotation
    ImmutableSet<String> annotationDefinedPatterns = ImmutableSet.copyOf(handlerAnnotation.value());

    if (!hasPatternsMapping(handlerAnnotation)) {
      return annotationDefinedPatterns;
    }

    // override with patterns from this config
    return ImmutableSet.copyOf(Sets.difference(
        Sets.union(annotationDefinedPatterns, getMethodConfig(handlerAnnotation, KEYNAME_PATTERNS)),
        getMethodConfig(handlerAnnotation, KEYNAME_EXCLUDEPATTERNS)));

  }

  private ImmutableSet<String> getMethodConfig(RequestMapping handlerAnnotation, String key) {
    return getMethodConfig(handlerAnnotation, key, ImmutableSet.<String>of());
  }

  private ImmutableSet<String> getMethodConfig(RequestMapping handlerAnnotation, String key, Collection<String> defaultVal) {
    String handlerName = getHandlerName(handlerAnnotation);
    List<String> methodVal = (List<String>) handlerMapping.get(handlerName).get(key);
    return methodVal != null ? ImmutableSet.copyOf(methodVal) : ImmutableSet.copyOf(defaultVal);
  }

  private ImmutableSet<String> getSiteOverride(RequestMapping handlerAnnotation, String siteKey, String key) {
    Set<String> overrideValue = new HashSet<>();
    if (hasSiteOverrides(handlerAnnotation)) {
      String handlerName = getHandlerName(handlerAnnotation);
      List<Map<String, List<String>>> siteOverrides =
          (List<Map<String, List<String>>>) handlerMapping.get(handlerName).get(KEYNAME_SITEOVERRIDES);
      for (Map<String, List<String>> siteOverride : siteOverrides) {
        if (siteOverride.get(KEYNAME_SITES) == null) {
          throw new RuntimeConfigurationException(
              String.format("HandlerMappingConfiguration ERROR: Site override handler mappings for " +
                  "method \"%s\" must all include a \"%s\" key value", handlerName, KEYNAME_SITES));
        }
        if ((siteOverride.get(KEYNAME_SITES)).contains(siteKey) && siteOverride.containsKey(key)) {
          overrideValue.addAll(siteOverride.get(key));
        }
      }
    }
    return ImmutableSet.copyOf(overrideValue);
  }

  private String getHandlerName(RequestMapping handlerAnnotation) {
    return handlerAnnotation.name();
  }

  private RequestMapping getRequestMapping(String handlerName) {
    HandlerMethod handlerMethod = handlerMethods.get(handlerName);
    if (handlerMethod == null) {
      throw new RuntimeConfigurationException(String.format("HandlerMappingConfiguration ERROR: " +
          "Requested handler mapping configuration for non-existing handler: \"%s\"", handlerName));
    }
    return AnnotationUtils.findAnnotation(handlerMethod.getMethod(), RequestMapping.class);
  }

  public void setHandlerMethods(Map<String, HandlerMethod> handlerMethods) {
    this.handlerMethods = ImmutableMap.copyOf(Preconditions.checkNotNull(handlerMethods));
  }

}