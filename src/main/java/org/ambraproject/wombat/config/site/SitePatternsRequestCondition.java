package org.ambraproject.wombat.config.site;


import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.ambraproject.wombat.config.HandlerMappingConfiguration;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.springframework.web.servlet.mvc.condition.CompositeRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SitePatternsRequestCondition extends CompositeRequestCondition {

  private SiteResolver siteResolver;
  private List<PatternsRequestCondition> patternsRCList;

  public SitePatternsRequestCondition(HandlerMappingConfiguration handlerMappingConfiguration, SiteResolver siteResolver){
    this.siteResolver = Preconditions.checkNotNull(siteResolver);
    this.patternsRCList = setPatternsRCList(
            Preconditions.checkNotNull(handlerMappingConfiguration));
  }

  private List<PatternsRequestCondition> setPatternsRCList(HandlerMappingConfiguration handlerMappingConfiguration) {

  }

}
