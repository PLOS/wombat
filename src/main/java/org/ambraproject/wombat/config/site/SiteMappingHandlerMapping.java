package org.ambraproject.wombat.config.site;

import org.ambraproject.wombat.config.HandlerMappingConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.condition.CompositeRequestCondition;
import org.springframework.web.servlet.mvc.condition.ConsumesRequestCondition;
import org.springframework.web.servlet.mvc.condition.HeadersRequestCondition;
import org.springframework.web.servlet.mvc.condition.ParamsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.ProducesRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestConditionHolder;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;


/**
 * Handler mapping class that incorporates custom {@link SiteRequestCondition}
 */
public class SiteMappingHandlerMapping extends RequestMappingHandlerMapping {

  @Autowired
  SiteResolver siteResolver;

  @Autowired
  SiteSet siteSet;

  @Autowired
  HandlerMappingConfiguration handlerMappingConfiguration;

  @Override
  protected RequestCondition<?> getCustomTypeCondition(Class<?> handlerType) {
    return null; // class level site mapping is not supported since the configuration is supplied at the method level
  }

  @Override
  protected RequestCondition<?> getCustomMethodCondition(Method method) {
    RequestMapping methodAnnotation = AnnotationUtils.findAnnotation(method, RequestMapping.class);
    if (methodAnnotation == null) {
      return null;
    }
    SitePatternsRequestCondition sitePatternsRC = null;
    SiteRequestCondition siteRC = null;
    if (handlerMappingConfiguration.hasSiteOverrides(methodAnnotation)) {
      sitePatternsRC = SitePatternsRequestCondition.create(handlerMappingConfiguration,
              methodAnnotation, siteResolver, handlerMappingConfiguration.getValidSites(methodAnnotation));
    } else if (handlerMappingConfiguration.hasSiteMapping(methodAnnotation)) {
      siteRC = new SiteRequestCondition(siteResolver, handlerMappingConfiguration.getValidSites(methodAnnotation));
    }
    return new CompositeRequestCondition(new RequestConditionHolder(siteRC),
            new RequestConditionHolder(sitePatternsRC));
  }

  @Override
  /**
   * Created a RequestMappingInfo from a RequestMapping annotation.
   */
  protected RequestMappingInfo createRequestMappingInfo(RequestMapping annotation, RequestCondition<?> customCondition) {
    Set<String> allPatterns = new HashSet<>();
    if (handlerMappingConfiguration.hasSiteOverrides(annotation)) {
      for (String siteKey : siteSet.getSiteKeys()) {
        allPatterns.addAll(handlerMappingConfiguration.getValidPatternsForSite(annotation, siteKey));
      }
    } else {
      allPatterns.addAll(handlerMappingConfiguration.getSharedPatterns(annotation));
    }
    String[] patterns = resolveEmbeddedValuesInPatterns(allPatterns.toArray(new String[allPatterns.size()]));
    return new RequestMappingInfo(
            annotation.name(),
            new PatternsRequestCondition(patterns, null, null, true, true, null),
            new RequestMethodsRequestCondition(annotation.method()),
            new ParamsRequestCondition(annotation.params()),
            new HeadersRequestCondition(annotation.headers()),
            new ConsumesRequestCondition(annotation.consumes(), annotation.headers()),
            new ProducesRequestCondition(annotation.produces(), annotation.headers(), null),
            customCondition);
  }


}
