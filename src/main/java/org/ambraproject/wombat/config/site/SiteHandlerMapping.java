package org.ambraproject.wombat.config.site;

import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.condition.ConsumesRequestCondition;
import org.springframework.web.servlet.mvc.condition.HeadersRequestCondition;
import org.springframework.web.servlet.mvc.condition.ParamsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.ProducesRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;


/**
 * Handler mapping class that incorporates custom {@link SiteRequestCondition}
 */
public class SiteHandlerMapping extends RequestMappingHandlerMapping {

  @Autowired
  SiteResolver siteResolver;
  @Autowired
  RequestHandlerPatternDictionary requestHandlerPatternDictionary;
  @Autowired
  SiteSet siteSet;

  @Override
  protected RequestCondition<?> getCustomTypeCondition(Class<?> handlerType) {
    return null; // class level site mapping is not supported since the configuration is supplied at the method level
  }

  @Override
  protected RequestCondition<?> getCustomMethodCondition(Method method) {
    RequestMapping methodAnnotation = AnnotationUtils.findAnnotation(method, RequestMapping.class);
    Preconditions.checkNotNull(methodAnnotation, "No @RequestMapping found on mapped method");
    return SiteRequestCondition.create(siteResolver, siteSet, method, requestHandlerPatternDictionary);
  }

  @Override
  protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
    RequestMappingInfo info = null;
    RequestMappingValue methodAnnotation = RequestMappingValue.create(method);
    if (methodAnnotation != null) {
      RequestCondition<?> methodCondition = this.getCustomMethodCondition(method);
      info = this.createRequestMappingInfo(methodAnnotation, methodCondition);
      RequestMappingValue typeAnnotation = RequestMappingValue.create(handlerType);
      if (typeAnnotation != null) {
        RequestCondition<?> typeCondition = this.getCustomTypeCondition(handlerType);
        info = this.createRequestMappingInfo(typeAnnotation, typeCondition).combine(info);
      }
    }
    return info;
  }

  @Override
  protected RequestMappingInfo createRequestMappingInfo(RequestMapping annotation, RequestCondition<?> customCondition) {
    // Kludge alert: We expect our override of getMappingForMethod
    // to shield this method from all calls in the superclass
    throw new UnsupportedOperationException();
  }

  private RequestMappingInfo createRequestMappingInfo(RequestMappingValue mapping, RequestCondition<?> customCondition) {
    Set<String> allPatterns = SiteRequestCondition.getAllPatterns(siteSet, mapping);
    RequestMapping annotation = mapping.getAnnotation();
    String[] embeddedPatterns = resolveEmbeddedValuesInPatterns(allPatterns.toArray(new String[allPatterns.size()]));
    return new RequestMappingInfo(
        annotation.name(),
        new PatternsRequestCondition(embeddedPatterns, null, null, true, true, null),
        new RequestMethodsRequestCondition(annotation.method()),
        new ParamsRequestCondition(annotation.params()),
        new HeadersRequestCondition(annotation.headers()),
        new ConsumesRequestCondition(annotation.consumes(), annotation.headers()),
        new ProducesRequestCondition(annotation.produces(), annotation.headers(), null),
        customCondition);
  }


}
