package org.ambraproject.wombat.config;

import org.ambraproject.wombat.model.TaxonomyCountTable;
import org.ambraproject.wombat.model.TaxonomyGraph;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CacheManagerWrapper implements ServiceCacheSet {
  private CacheManager manager;

  CacheManagerWrapper() {
    CachingProvider provider = Caching.getCachingProvider();
    manager = provider.getCacheManager();
    createCaches();
  }

  private void createCaches() {
    MutableConfiguration<String, String> configuration1 = new MutableConfiguration<String, String>();
    configuration1.setTypes(String.class, String.class);
    configuration1.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 15)));
    manager.createCache("assetFilenameCache", configuration1);

    MutableConfiguration<String, Object> configuration2 = new MutableConfiguration<String, Object>();
    configuration2.setTypes(String.class, Object.class);
    configuration2.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 15)));
    manager.createCache("assetContentCache", configuration2);

    MutableConfiguration<String, TaxonomyGraph> configuration3 = new MutableConfiguration<String, TaxonomyGraph>();
    configuration3.setTypes(String.class, TaxonomyGraph.class);
    manager.createCache("taxonomyGraphCache", configuration3);

    MutableConfiguration<String, TaxonomyCountTable> configuration4 = new MutableConfiguration<String, TaxonomyCountTable>();
    configuration4.setTypes(String.class, TaxonomyCountTable.class);
    manager.createCache("taxonomyCountTableCache", configuration4);

    MutableConfiguration<String, List> configuration5 = new MutableConfiguration<String, List>();
    configuration5.setTypes(String.class, List.class);
    configuration5.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 30)));
    manager.createCache("recentArticleCache", configuration5);

    MutableConfiguration<String, Object> configuration6 = new MutableConfiguration<String, Object>();
    configuration6.setTypes(String.class, Object.class);
    configuration6.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.HOURS, 1)));
    manager.createCache("remoteServiceCache", configuration6);
  }

  @Override
  public Cache<String, String> getAssetFilenameCache() {
    return manager.getCache("assetFilenameCache", String.class, String.class);
  }

  @Override
  public Cache<String, Object> getAssetContentCache() {
    return manager.getCache("assetContentCache", String.class, Object.class);
  }

  @Override
  public Cache<String, TaxonomyGraph> getTaxonomyGraphCache() {
    return manager.getCache("taxonomyGraphCache", String.class, TaxonomyGraph.class);
  }

  @Override
  public Cache<String, TaxonomyCountTable> getTaxonomyCountTableCache() {
    return manager.getCache("taxonomyCountTableCache", String.class, TaxonomyCountTable.class);
  }

  @Override
  public Cache<String, List> getRecentArticleCache() {
    return manager.getCache("recentArticleCache", String.class, List.class);
  }

  @Override
  public Cache<String, Object> getRemoteServiceCache() {
    return manager.getCache("remoteServiceCache", String.class, Object.class);
  }
}
