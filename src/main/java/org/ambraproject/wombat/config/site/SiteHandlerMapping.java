package org.ambraproject.wombat.config.site;

import org.ambraproject.wombat.controller.RootController;
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
import java.util.Set;


/**
 * Handler mapping class that incorporates custom {@link SiteRequestCondition}
 */
public class SiteHandlerMapping extends RequestMappingHandlerMapping {

  @Autowired
  SiteResolver siteResolver;
  @Autowired
  HandlerDirectory handlerDirectory;
  @Autowired
  SiteSet siteSet;

  @Override
  protected RequestCondition<?> getCustomTypeCondition(Class<?> handlerType) {
    return null; // class level site mapping is not supported since the configuration is supplied at the method level
  }

  @Override
  protected RequestCondition<?> getCustomMethodCondition(Method method) {
    RequestMapping methodAnnotation = AnnotationUtils.findAnnotation(method, RequestMapping.class);
    if (method.getDeclaringClass().equals(RootController.class)) {
      return handleRootController();
    }
    if (methodAnnotation == null) {
      return null;
    }
    if (methodAnnotation.value().length == 0) {
      return null;
    }
    return SiteRequestCondition.create(siteResolver, siteSet, methodAnnotation, handlerDirectory);
  }

  /**
   * Set up a special request condition for {@link RootController}.
   * <p/>
   * {@link RootController} is the only pattern-matched page that does not belong to a {@link Site}. ({@link
   * org.ambraproject.wombat.controller.NotFoundController} also does not belong to a site, but it is not
   * pattern-matched.) Therefore, it is a special case that we handle uniquely here.
   * <p/>
   * In general, we want to serve the root page if the servlet root is not mapped to one or more sites (assuming that
   * the sites would be served according to special hostnames). Otherwise, we assume that the "/" pattern will be mapped
   * to {@link org.ambraproject.wombat.controller.HomeController#serveHomepage} by default or something else by
   * override.
   *
   * @return a request condition for the application root pattern if it is available, or {@code null} if that pattern
   * risks a collision
   */
  private RequestCondition<?> handleRootController() {
    boolean allSitesHavePathToken = true;
    for (Site site : siteSet.getSites()) {
      if (!site.getRequestScheme().hasPathToken()) {
        allSitesHavePathToken = false;
        break;
      }
    }
    return allSitesHavePathToken ? new PatternsRequestCondition(new String[]{"/"}, null, null, false, false, null) : null;
  }

  /**
   * Create a RequestMappingInfo from a RequestMapping annotation.
   */
  @Override
  protected RequestMappingInfo createRequestMappingInfo(RequestMapping annotation, RequestCondition<?> customCondition) {
    Set<String> allPatterns = SiteRequestCondition.getAllPatterns(siteSet, annotation);
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
