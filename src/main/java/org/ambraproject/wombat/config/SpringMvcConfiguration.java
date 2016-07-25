package org.ambraproject.wombat.config;

import org.ambraproject.wombat.config.site.SiteHandlerMapping;
import org.ambraproject.wombat.config.site.SiteResolver;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.controller.DoiVersionArgumentResolver;
import org.ambraproject.wombat.service.AssetService;
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
public class SpringMvcConfiguration extends WebMvcConfigurationSupport {

  @Autowired
  SiteResolver siteResolver;
  @Autowired
  DoiVersionArgumentResolver doiVersionArgumentResolver;

  @Autowired
  SiteSet siteSet;

  @Bean
  public RequestMappingHandlerMapping requestMappingHandlerMapping() {
    RequestMappingHandlerMapping handlerMapping = new SiteHandlerMapping();
    handlerMapping.setOrder(0);
    handlerMapping.setInterceptors(getInterceptors());
    handlerMapping.setContentNegotiationManager(mvcContentNegotiationManager());
    return handlerMapping;
  }

  @Override
  protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    argumentResolvers.add(siteResolver);
    argumentResolvers.add(doiVersionArgumentResolver);
  }

  @Override
  protected void addResourceHandlers(ResourceHandlerRegistry registry) {
    ResourceHandlerRegistration registration = registry.addResourceHandler(AssetService.AssetUrls.RESOURCE_TEMPLATE);
    registration.addResourceLocations("/" + AssetService.AssetUrls.RESOURCE_NAMESPACE + "/");
  }

}