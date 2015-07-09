package org.ambraproject.wombat.config;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.ambraproject.wombat.config.site.SiteSet;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class HandlerMappingConfiguration {

  private static final String KEYNAME_SITES = "sites";
  private static final String KEYNAME_EXCLUDESITES = "excludeSites";
  private static final String KEYNAME_PATTERNS = "patterns";
  private static final String KEYNAME_SITEPATTERNS = "sitePatterns";

  private Map<String, Map<String, ?>> handlerMapping;

  public HandlerMappingConfiguration(Map<String, Map<String, ?>> handlerMapping) {
    this.handlerMapping = handlerMapping;
  }

  public boolean hasSiteMapping(Method method) {
    String handlerName = getHandlerName(method);
    if (handlerMapping.get(handlerName) != null && (handlerMapping.get(handlerName).get(KEYNAME_SITES) != null ||
            handlerMapping.get(handlerName).get(KEYNAME_EXCLUDESITES) != null)) {
      return true;
    }
    return false;
  }

  public ImmutableSet<String> getValidSites(SiteSet siteSet, Method method) {
    String handlerName = getHandlerName(method);
    if (!hasSiteMapping(method)) {
      return siteSet.getSiteKeys();
    }

    List<String> includedSites = (List<String>) handlerMapping.get(handlerName).get(KEYNAME_SITES);
    List<String> excludedSites = (List<String>) handlerMapping.get(handlerName).get(KEYNAME_EXCLUDESITES);

    return ImmutableSet.copyOf(Sets.difference(
            includedSites != null ? ImmutableSet.copyOf(includedSites) : siteSet.getSiteKeys(),
            excludedSites != null ? ImmutableSet.copyOf(excludedSites) : ImmutableSet.of()));
  }

  private String getHandlerName(Method method) {
    String fullClassName = method.getDeclaringClass().getName();
    String className = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
    return className + "#" + method.getName();
  }

}
