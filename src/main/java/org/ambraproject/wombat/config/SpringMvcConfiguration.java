package org.ambraproject.wombat.config;

import org.ambraproject.wombat.config.site.SiteMappingHandlerMapping;
import org.ambraproject.wombat.config.site.SiteResolver;
import org.ambraproject.wombat.config.site.SiteSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

@Configuration
public class SpringMvcConfiguration extends WebMvcConfigurationSupport{

  @Autowired
  SiteResolver siteResolver;

  @Autowired
  SiteSet siteSet;

  @Bean
  public RequestMappingHandlerMapping requestMappingHandlerMapping() {
    RequestMappingHandlerMapping handlerMapping = new SiteMappingHandlerMapping();
    handlerMapping.setOrder(0);
    handlerMapping.setInterceptors(getInterceptors());
    handlerMapping.setContentNegotiationManager(mvcContentNegotiationManager());
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