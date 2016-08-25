package org.ambraproject.wombat.config;

import org.ambraproject.wombat.model.TaxonomyCountTable;
import org.ambraproject.wombat.model.TaxonomyGraph;
import org.ambraproject.wombat.service.remote.RemoteCacheKey;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Expirations;
import org.ehcache.jsr107.Eh107Configuration;
import org.ehcache.expiry.Duration;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

class CacheManagerWrapper implements ServiceCacheSet {

  private final CacheManager manager;

  private static <K, V> Cache<K, V> constructCache(CacheManager manager, String cacheName,
        Class<K> keyType, Class<V> valueType, long entries, long ttl, TimeUnit unit) {

    // see http://www.ehcache.org/documentation/3.1/107.html "The Ehcache 3.x JSR-107 Provider"
    // section: "Building the configuration using Ehcache APIs"
    CacheConfigurationBuilder cacheBuilder = CacheConfigurationBuilder.newCacheConfigurationBuilder(keyType, valueType, ResourcePoolsBuilder.heap(entries))
        .withExpiry(Expirations.timeToLiveExpiration(Duration.of(ttl, unit)));
    return manager.createCache(cacheName, Eh107Configuration.fromEhcacheCacheConfiguration(cacheBuilder.build()));
  }

  private static final String ASSET_FILENAME_CACHE = "assetFilename";
  private static final String ASSET_CONTENT_CACHE = "assetContent";
  private static final String TAXONOMY_GRAPH_CACHE = "taxonomyGraph";
  private static final String TAXONOMY_COUNT_TABLE_CACHE = "taxonomyCountTable";
  private static final String RECENT_ARTICLE_CACHE = "recentArticle";

  private static Properties getCacheManagerProperties(RuntimeConfiguration configuration) {
    RuntimeConfiguration.CacheConfiguration cacheConfiguration = configuration.getCacheConfiguration();
    Properties properties = new Properties();
    properties.setProperty("maxBytesLocalHeap", cacheConfiguration.getMaxBytesLocalHeap());
    return properties;
  }

  CacheManagerWrapper(RuntimeConfiguration configuration) {
    Properties properties = getCacheManagerProperties(configuration);
    manager = Caching.getCachingProvider().getCacheManager(null, null, properties);

    constructCache(manager, ASSET_FILENAME_CACHE, String.class, String.class,
        10_000, 15, TimeUnit.MINUTES);
    constructCache(manager, ASSET_CONTENT_CACHE, String.class, Object.class,
        10_000, 15, TimeUnit.MINUTES);
    constructCache(manager, TAXONOMY_GRAPH_CACHE, String.class, TaxonomyGraph.class,
        10_000, 1, TimeUnit.HOURS);
    constructCache(manager, TAXONOMY_COUNT_TABLE_CACHE, String.class, TaxonomyCountTable.class,
        10_000, 1, TimeUnit.HOURS);
    constructCache(manager, RECENT_ARTICLE_CACHE, String.class, List.class,
        10_000, 30, TimeUnit.MINUTES);

    for (RemoteCacheSpace rcs : RemoteCacheSpace.values()) {
      constructCache(manager, rcs.getCacheName(), rcs.getKeyClass(),
          Object.class, rcs.getEntries(), rcs.getTimeToLive(), rcs.getTTLUnits());
    }
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
  public Cache<RemoteCacheKey, Object> getCacheFor(RemoteCacheSpace space) {
    return manager.getCache(space.getCacheName(), RemoteCacheKey.class, Object.class);
  }
}
