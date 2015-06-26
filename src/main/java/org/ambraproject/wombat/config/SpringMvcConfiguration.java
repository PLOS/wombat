package org.ambraproject.wombat.config;

import org.ambraproject.wombat.controller.SiteResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

@Configuration
public class SpringMvcConfiguration extends WebMvcConfigurationSupport{

  @Autowired
  SiteResolver siteResolver;

  @Bean
  public RequestMappingHandlerMapping requestMappingHandlerMapping() {
    RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();
    handlerMapping.setOrder(0);
    handlerMapping.setInterceptors(getInterceptors());
    handlerMapping.setContentNegotiationManager(mvcContentNegotiationManager());

    PathMatchConfigurer configurer = getPathMatchConfigurer();
    if (configurer.isUseSuffixPatternMatch() != null) {
      handlerMapping.setUseSuffixPatternMatch(configurer.isUseSuffixPatternMatch());
    }
    if (configurer.isUseRegisteredSuffixPatternMatch() != null) {
      handlerMapping.setUseRegisteredSuffixPatternMatch(configurer.isUseRegisteredSuffixPatternMatch());
    }
    if (configurer.isUseTrailingSlashMatch() != null) {
      handlerMapping.setUseTrailingSlashMatch(configurer.isUseTrailingSlashMatch());
    }
    if (configurer.getPathMatcher() != null) {
      handlerMapping.setPathMatcher(configurer.getPathMatcher());
    }
    if (configurer.getUrlPathHelper() != null) {
      handlerMapping.setUrlPathHelper(configurer.getUrlPathHelper());
    }
    return handlerMapping;
  }

  @Override
  protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    argumentResolvers.add(siteResolver);
  }

  @Override
  protected void addResourceHandlers(ResourceHandlerRegistry registry) {
    ResourceHandlerRegistration registration = registry.addResourceHandler("/resources/**");
    registration.addResourceLocations("/resources/");
  }
}