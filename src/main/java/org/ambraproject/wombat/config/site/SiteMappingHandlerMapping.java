package org.ambraproject.wombat.config.site;

import com.google.common.collect.ImmutableSet;
import org.ambraproject.wombat.config.HandlerMappingConfiguration;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
    if (methodAnnotation == null || !handlerMappingConfiguration.hasSiteMapping(method)) {
      return null;
    }
    return new SiteRequestCondition(siteResolver, handlerMappingConfiguration.getValidSites(siteSet, method));
  }
}
