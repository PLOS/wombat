package org.ambraproject.wombat.config;

import org.ambraproject.wombat.model.TaxonomyCountTable;
import org.ambraproject.wombat.model.TaxonomyGraph;
import org.ambraproject.wombat.service.remote.RemoteCacheKey;
import org.ambraproject.wombat.util.DummyCache;

import javax.cache.Cache;
import java.util.List;

public class DummyServiceCacheSet implements ServiceCacheSet {

  @Override
  public Cache<String, String> getAssetFilenameCache() {
    return DummyCache.getInstance();
  }

  @Override
  public Cache<String, Object> getAssetContentCache() {
    return DummyCache.getInstance();
  }

  @Override
  public Cache<String, TaxonomyGraph> getTaxonomyGraphCache() {
    return DummyCache.getInstance();
  }

  @Override
  public Cache<String, TaxonomyCountTable> getTaxonomyCountTableCache() {
    return DummyCache.getInstance();
  }

  @Override
  public Cache<String, List> getRecentArticleCache() {
    return DummyCache.getInstance();
  }

  @Override
  public Cache<RemoteCacheKey, Object> getArticleApiCache() {
    return DummyCache.getInstance();
  }

  @Override
  public Cache<RemoteCacheKey, Object> getUserApiCache() {
    return DummyCache.getInstance();
  }

  @Override
  public Cache<RemoteCacheKey, Object> getArticleHtmlCache() {
    return DummyCache.getInstance();
  }

  @Override
  public Cache<RemoteCacheKey, Object> getAmendmentBodyCache() {
    return DummyCache.getInstance();
  }

  @Override
  public Cache<RemoteCacheKey, Object> getSiteContentMetadataCache() {
    return DummyCache.getInstance();
  }

  @Override
  public Cache<RemoteCacheKey, Object> getExternalResourceCache() {
    return DummyCache.getInstance();
  }

  @Override
  public Cache<RemoteCacheKey, Object> getEditorialContentCache() {
    return DummyCache.getInstance();
  }
}
