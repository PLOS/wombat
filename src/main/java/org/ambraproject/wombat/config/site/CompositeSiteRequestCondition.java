package org.ambraproject.wombat.config.site;


import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.ambraproject.wombat.config.HandlerMappingConfiguration;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.springframework.web.servlet.mvc.condition.CompositeRequestCondition;

import java.util.Set;

public class CompositeSiteRequestCondition extends CompositeRequestCondition {

  private SiteResolver siteResolver;
  private SiteSet siteSet;
  private HandlerMappingConfiguration handlerMappingConfiguration;

  private CompositeSiteRequestCondition(HandlerMappingConfiguration handlerMappingConfiguration, SiteSet siteSet, SiteResolver siteResolver){
    this.siteSet = Preconditions.checkNotNull(siteSet);
    this.siteResolver = Preconditions.checkNotNull(siteResolver);
    this.handlerMappingConfiguration = Preconditions.checkNotNull(handlerMappingConfiguration);
  }
}
