package org.ambraproject.wombat.config.site;


import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.ambraproject.wombat.config.HandlerMappingConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestCondition;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;


public class SitePatternsRequestCondition implements RequestCondition<SitePatternsRequestCondition> {

  private final SiteResolver siteResolver;
  private final ImmutableMap<String, PatternsRequestCondition> requestConditionMap;

  private SitePatternsRequestCondition(SiteResolver siteResolver, Map<String, PatternsRequestCondition> requestConditionMap) {
    this.siteResolver = Preconditions.checkNotNull(siteResolver);
    this.requestConditionMap = ImmutableMap.copyOf(requestConditionMap);
  }

  private static ImmutableMap<String, PatternsRequestCondition> buildRequestConditionMap(
      HandlerMappingConfiguration handlerMappingConfiguration,
      RequestMapping handlerAnnotation,
      Set<String> siteKeys) {
    Preconditions.checkNotNull(handlerMappingConfiguration);
    Preconditions.checkNotNull(handlerAnnotation);

    ImmutableMap.Builder<String, PatternsRequestCondition> mapBuilder = ImmutableMap.builder();
    for (String siteKey : siteKeys) {
      Set<String> patterns = handlerMappingConfiguration.getValidPatternsForSite(handlerAnnotation, siteKey);
      PatternsRequestCondition condition = new PatternsRequestCondition(patterns.toArray(new String[patterns.size()]), null, null, true, true, null);
      mapBuilder.put(siteKey, condition);
    }
    return mapBuilder.build();
  }

  public static SitePatternsRequestCondition create(HandlerMappingConfiguration handlerMappingConfiguration,
                                                    RequestMapping handlerAnnotation,
                                                    SiteResolver siteResolver,
                                                    Set<String> siteKeys) {
    return new SitePatternsRequestCondition(siteResolver,
        buildRequestConditionMap(handlerMappingConfiguration, handlerAnnotation, siteKeys));
  }

  @Override
  public SitePatternsRequestCondition combine(SitePatternsRequestCondition other) {
    Set<String> siteKeys = Sets.union(requestConditionMap.keySet(), other.requestConditionMap.keySet());
    ImmutableMap.Builder<String, PatternsRequestCondition> mapBuilder = ImmutableMap.builder();
    for (String siteKey : siteKeys) {
      PatternsRequestCondition thisRC = requestConditionMap.get(siteKey);
      PatternsRequestCondition otherRC = other.requestConditionMap.get(siteKey);
      if (otherRC == null) {
        mapBuilder.put(siteKey, thisRC);
      } else if (thisRC == null) {
        mapBuilder.put(siteKey, otherRC);
      } else {
        mapBuilder.put(siteKey, thisRC.combine(otherRC));
      }
    }
    return new SitePatternsRequestCondition(siteResolver, mapBuilder.build());
  }

  @Override
  public SitePatternsRequestCondition getMatchingCondition(HttpServletRequest request) {
    Site site = siteResolver.resolveSite(request);
    String siteKey = site != null ? site.getKey() : HandlerMappingConfiguration.VALUE_NULLSITE;
    if (requestConditionMap.get(siteKey) == null) {
      return null;
    }
    PatternsRequestCondition patternsRC = requestConditionMap.get(siteKey).getMatchingCondition(request);
    if (patternsRC == null) {
      return null;
    }
    return new SitePatternsRequestCondition(siteResolver, ImmutableMap.of(siteKey, patternsRC));
  }

  @Override
  public int compareTo(SitePatternsRequestCondition other, HttpServletRequest request) {
    Site site = siteResolver.resolveSite(request);
    String siteKey = site != null ? site.getKey() : HandlerMappingConfiguration.VALUE_NULLSITE;
    PatternsRequestCondition thisRC = requestConditionMap.get(siteKey);
    PatternsRequestCondition otherRC = other.requestConditionMap.get(siteKey);
    if (thisRC == null && otherRC == null) {
      return 0;
    } else if (thisRC == null) {
      return 1;
    } else if (otherRC == null) {
      return -1;
    } else {
      return thisRC.compareTo(otherRC, request);
    }
  }
}
