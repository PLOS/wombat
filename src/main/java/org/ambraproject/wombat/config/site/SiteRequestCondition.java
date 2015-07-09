package org.ambraproject.wombat.config.site;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * custom {@link org.springframework.web.servlet.mvc.condition.RequestCondition} used for mapping sites to
 * controller methods
 */
public class SiteRequestCondition implements RequestCondition <SiteRequestCondition> {

  private final ImmutableSet<String> validSites;
  private SiteResolver siteResolver;

  public SiteRequestCondition(SiteResolver siteResolver, Set<String> validSites){
    this.siteResolver = Preconditions.checkNotNull(siteResolver);
    this.validSites = ImmutableSet.copyOf(Preconditions.checkNotNull(validSites));
  }

  @Override
  public SiteRequestCondition getMatchingCondition(HttpServletRequest request) {

    Site site = siteResolver.resolveSite(request);
    if (site == null) {
      return null;
    }
    if (validSites.contains(site.getKey())) {
      return this;
    }
    return null;
  }

  @Override
  public SiteRequestCondition combine(SiteRequestCondition other) {
    return new SiteRequestCondition(siteResolver, ImmutableSet.copyOf(Sets.intersection(validSites, other.validSites)));
  }


  @Override
  public int compareTo(SiteRequestCondition other, HttpServletRequest request) {
    return 0;
  }
}
