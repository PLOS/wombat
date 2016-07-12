package org.ambraproject.wombat.config;

import org.ambraproject.wombat.model.TaxonomyCountTable;
import org.ambraproject.wombat.model.TaxonomyGraph;
import org.ambraproject.wombat.service.remote.RemoteCacheKey;

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

  private static Cache<RemoteCacheKey, Object> constructDefaultRemoteCache(CacheManager manager, String cacheName) {
    return constructCache(manager, cacheName, RemoteCacheKey.class, Object.class, config -> {
      config.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.HOURS, 1)));
    });
  }


  private static final String ASSET_FILENAME_CACHE = "assetFilenameCache";
  private static final String ASSET_CONTENT_CACHE = "assetContentCache";
  private static final String TAXONOMY_GRAPH_CACHE = "taxonomyGraphCache";
  private static final String TAXONOMY_COUNT_TABLE_CACHE = "taxonomyCountTableCache";
  private static final String RECENT_ARTICLE_CACHE = "recentArticleCache";
  private static final String ARTICLE_API_CACHE = "articleApiCache";
  private static final String USER_API_CACHE = "userApiCache";
  private static final String ARTICLE_HTML_CACHE = "articleHtmlCache";
  private static final String AMENDMENT_BODY_CACHE = "amendmentBodyCache";
  private static final String SITE_CONTENT_METADATA_CACHE = "siteContentMetadataCache";
  private static final String EXTERNAL_RESOURCE_CACHE = "externalResourceCache";
  private static final String EDITORIAL_CONTENT_CACHE = "editorialContentCache";

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

    constructDefaultRemoteCache(manager, ARTICLE_API_CACHE);
    constructDefaultRemoteCache(manager, USER_API_CACHE);
    constructDefaultRemoteCache(manager, ARTICLE_HTML_CACHE);
    constructDefaultRemoteCache(manager, AMENDMENT_BODY_CACHE);
    constructDefaultRemoteCache(manager, SITE_CONTENT_METADATA_CACHE);
    constructDefaultRemoteCache(manager, EXTERNAL_RESOURCE_CACHE);

    constructCache(manager, EDITORIAL_CONTENT_CACHE, RemoteCacheKey.class, Object.class, config -> {
      config.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(new Duration(TimeUnit.MINUTES, 30)));
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
  public Cache<RemoteCacheKey, Object> getArticleApiCache() {
    return manager.getCache(ARTICLE_API_CACHE, RemoteCacheKey.class, Object.class);
  }

  @Override
  public Cache<RemoteCacheKey, Object> getUserApiCache() {
    return manager.getCache(USER_API_CACHE, RemoteCacheKey.class, Object.class);
  }

  @Override
  public Cache<RemoteCacheKey, Object> getArticleHtmlCache() {
    return manager.getCache(ARTICLE_HTML_CACHE, RemoteCacheKey.class, Object.class);
  }

  @Override
  public Cache<RemoteCacheKey, Object> getAmendmentBodyCache() {
    return manager.getCache(AMENDMENT_BODY_CACHE, RemoteCacheKey.class, Object.class);
  }

  @Override
  public Cache<RemoteCacheKey, Object> getSiteContentMetadataCache() {
    return manager.getCache(SITE_CONTENT_METADATA_CACHE, RemoteCacheKey.class, Object.class);
  }

  @Override
  public Cache<RemoteCacheKey, Object> getExternalResourceCache() {
    return manager.getCache(EXTERNAL_RESOURCE_CACHE, RemoteCacheKey.class, Object.class);
  }

  @Override
  public Cache<RemoteCacheKey, Object> getEditorialContentCache() {
    return manager.getCache(EDITORIAL_CONTENT_CACHE, RemoteCacheKey.class, Object.class);
  }
}
