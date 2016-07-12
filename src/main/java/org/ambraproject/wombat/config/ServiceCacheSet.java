package org.ambraproject.wombat.config;

import org.ambraproject.wombat.model.TaxonomyCountTable;
import org.ambraproject.wombat.model.TaxonomyGraph;
import org.ambraproject.wombat.service.remote.RemoteCacheKey;

import javax.cache.Cache;
import java.util.List;

public interface ServiceCacheSet {
  Cache<String, String> getAssetFilenameCache();

  Cache<String, Object> getAssetContentCache();

  Cache<String, TaxonomyGraph> getTaxonomyGraphCache();

  Cache<String, TaxonomyCountTable> getTaxonomyCountTableCache();

  Cache<String, List> getRecentArticleCache();

  Cache<RemoteCacheKey, Object> getArticleApiCache();

  Cache<RemoteCacheKey, Object> getUserApiCache();

  Cache<RemoteCacheKey, Object> getArticleHtmlCache();

  Cache<RemoteCacheKey, Object> getAmendmentBodyCache();

  Cache<RemoteCacheKey, Object> getSiteContentMetadataCache();

  Cache<RemoteCacheKey, Object> getExternalResourceCache();

  Cache<RemoteCacheKey, Object> getEditorialContentCache();
}
