/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

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