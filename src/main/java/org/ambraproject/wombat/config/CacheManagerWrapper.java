package org.ambraproject.wombat.config;

import org.ambraproject.wombat.model.TaxonomyCountTable;
import org.ambraproject.wombat.model.TaxonomyGraph;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

class CacheManagerWrapper implements ServiceCacheSet {

  private final CacheManager manager;

  private static <K, V> Cache<K, V> constructCache(CacheManager manager, String cacheName,
                                                   Class<K> keyType, Class<V> valueType,
                                                   Consumer<MutableConfiguration<K, V>> configurationConsumer) {
    MutableConfiguration<K, V> configuration = new MutableConfiguration<>();
    configuration.setTypes(keyType, valueType);
    configurationConsumer.accept(configuration);
    return manager.createCache(cacheName, configuration);
  }


  private static final String ASSET_FILENAME_CACHE = "assetFilenameCache";
  private static final String ASSET_CONTENT_CACHE = "assetContentCache";
  private static final String TAXONOMY_GRAPH_CACHE = "taxonomyGraphCache";
  private static final String TAXONOMY_COUNT_TABLE_CACHE = "taxonomyCountTableCache";
  private static final String RECENT_ARTICLE_CACHE = "recentArticleCache";
  private static final String REMOTE_SERVICE_CACHE = "remoteServiceCache";

  CacheManagerWrapper() {
    manager = Caching.getCachingProvider().getCacheManager();

    constructCache(manager, ASSET_FILENAME_CACHE, String.class, String.class, config -> {
      config.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 15)));
    });

    constructCache(manager, ASSET_CONTENT_CACHE, String.class, Object.class, config -> {
      config.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 15)));
    });

    constructCache(manager, TAXONOMY_GRAPH_CACHE, String.class, TaxonomyGraph.class, config -> {
    });

    constructCache(manager, TAXONOMY_COUNT_TABLE_CACHE, String.class, TaxonomyCountTable.class, config -> {
    });

    constructCache(manager, RECENT_ARTICLE_CACHE, String.class, List.class, config -> {
      config.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 30)));
    });

    constructCache(manager, REMOTE_SERVICE_CACHE, String.class, Object.class, config -> {
      config.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.HOURS, 1)));
    });

  }

  @Override
  public Cache<String, String> getAssetFilenameCache() {
    return manager.getCache(ASSET_FILENAME_CACHE, String.class, String.class);
  }

  @Override
  public Cache<String, Object> getAssetContentCache() {
    return manager.getCache(ASSET_CONTENT_CACHE, String.class, Object.class);
  }

  @Override
  public Cache<String, TaxonomyGraph> getTaxonomyGraphCache() {
    return manager.getCache(TAXONOMY_GRAPH_CACHE, String.class, TaxonomyGraph.class);
  }

  @Override
  public Cache<String, TaxonomyCountTable> getTaxonomyCountTableCache() {
    return manager.getCache(TAXONOMY_COUNT_TABLE_CACHE, String.class, TaxonomyCountTable.class);
  }

  @Override
  public Cache<String, List> getRecentArticleCache() {
    return manager.getCache(RECENT_ARTICLE_CACHE, String.class, List.class);
  }

  @Override
  public Cache<String, Object> getRemoteServiceCache() {
    return manager.getCache(REMOTE_SERVICE_CACHE, String.class, Object.class);
  }
}
