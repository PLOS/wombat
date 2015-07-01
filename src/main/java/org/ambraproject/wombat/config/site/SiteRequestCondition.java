package org.ambraproject.wombat.config.site;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * custom {@link org.springframework.web.servlet.mvc.condition.RequestCondition} used for mapping sites to controller
 * classes and/or methods
 */
public class SiteRequestCondition implements RequestCondition <SiteRequestCondition> {

  private final ImmutableSet<String> includedSites;
  private final ImmutableSet<String> excludedSites;
  private SiteResolver siteResolver;
  private SiteSet siteSet;

  public SiteRequestCondition(SiteSet siteSet, SiteResolver siteResolver, Set<String> includedSites, Set<String> excludedSites){
    Preconditions.checkNotNull(siteResolver);
    Preconditions.checkNotNull(siteSet);
    Preconditions.checkNotNull(includedSites);
    this.siteSet = siteSet;
    this.siteResolver = siteResolver;
    this.includedSites = includedSites.isEmpty() ? siteSet.getSiteKeys() : ImmutableSet.copyOf(includedSites);
    this.excludedSites = excludedSites == null ? ImmutableSet.<String>of() : ImmutableSet.copyOf(excludedSites);
  }

  public SiteRequestCondition(SiteSet siteSet, SiteResolver siteResolver, Set<String> includedSites) {
    this(siteSet, siteResolver, includedSites, null);
  }

  @Override
  public SiteRequestCondition getMatchingCondition(HttpServletRequest request) {

    Site site = siteResolver.resolveSite(request);
    if (site == null) {
      return null;
    }
    String siteKey = site.getKey();
    if (includedSites.contains(siteKey) && !excludedSites.contains(siteKey)) {
      return this;
    }
    return null;
  }

  @Override
  public SiteRequestCondition combine(SiteRequestCondition other) {
    Set<String> combinedIncludedSites = new ImmutableSet.Builder<String>()
            .addAll(includedSites)
            .addAll(other.includedSites)
            .build();
    Set<String> combinedExcludedSites = new ImmutableSet.Builder<String>()
            .addAll(excludedSites)
            .addAll(other.excludedSites)
            .build();
    return new SiteRequestCondition(siteSet, siteResolver, combinedIncludedSites, combinedExcludedSites);
  }


  @Override
  public int compareTo(SiteRequestCondition other, HttpServletRequest request) {
    return 0;
  }
}
