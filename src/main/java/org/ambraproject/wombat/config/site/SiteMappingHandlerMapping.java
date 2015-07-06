package org.ambraproject.wombat.config.site;

import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;

/**
 * Handler mapping class that incorporates custom {@link SiteRequestCondition}
 */
public class SiteMappingHandlerMapping extends RequestMappingHandlerMapping {

  @Autowired
  SiteResolver siteResolver;

  @Autowired
  SiteSet siteSet;

  @Override
  protected RequestCondition<?> getCustomTypeCondition(Class<?> handlerType) {
    SiteMapping typeAnnotation = AnnotationUtils.findAnnotation(handlerType, SiteMapping.class);
    if (typeAnnotation == null) {
      return null;
    }
    if (typeAnnotation.value().length == 0) {
      return SiteRequestCondition.createByExcluded(siteSet, siteResolver,
          ImmutableSet.copyOf(typeAnnotation.excluded()));
    }
    return SiteRequestCondition.create(siteSet, siteResolver, ImmutableSet.copyOf(typeAnnotation.value()),
        ImmutableSet.copyOf(typeAnnotation.excluded()));
  }

  @Override
  protected RequestCondition<?> getCustomMethodCondition(Method method) {
    SiteMapping methodAnnotation = AnnotationUtils.findAnnotation(method, SiteMapping.class);
    if (methodAnnotation == null) {
      return null;
    }
    if (methodAnnotation.value().length == 0) {
       return SiteRequestCondition.createByExcluded(siteSet, siteResolver,
           ImmutableSet.copyOf(methodAnnotation.excluded()));
    }
    return SiteRequestCondition.create(siteSet, siteResolver, ImmutableSet.copyOf(methodAnnotation.value()),
        ImmutableSet.copyOf(methodAnnotation.excluded()));
  }
}
