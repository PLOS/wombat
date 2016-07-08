package org.ambraproject.wombat.util;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.integration.CompletionListener;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class DummyCache<K, V> implements Cache<K, V> {
  private DummyCache() {
  }

  private static final DummyCache<?, ?> INSTANCE = new DummyCache<>();

  @SuppressWarnings("unchecked")
  public static <K, V> DummyCache<K, V> getInstance() {
    return (DummyCache<K, V>) INSTANCE;
  }

  @Override
  public V get(K key) {
    Objects.requireNonNull(key);
    return null;
  }

  @Override
  public Map<K, V> getAll(Set<? extends K> keys) {
    keys.forEach(Objects::requireNonNull);
    return Collections.emptyMap();
  }

  @Override
  public boolean containsKey(K key) {
    return false;
  }

  @Override
  public void loadAll(Set<? extends K> keys, boolean replaceExistingValues, CompletionListener completionListener) {
    keys.forEach(Objects::requireNonNull);
  }

  @Override
  public void put(K key, V value) {
    Objects.requireNonNull(key);
    Objects.requireNonNull(value);
  }

  @Override
  public V getAndPut(K key, V value) {
    Objects.requireNonNull(key);
    Objects.requireNonNull(value);
    return null;
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> map) {
    map.entrySet().forEach(entry -> {
      Objects.requireNonNull(entry.getKey());
      Objects.requireNonNull(entry.getValue());
    });
  }

  @Override
  public boolean putIfAbsent(K key, V value) {
    Objects.requireNonNull(key);
    Objects.requireNonNull(value);
    return false;
  }

  @Override
  public boolean remove(K key) {
    Objects.requireNonNull(key);
    return false;
  }

  @Override
  public boolean remove(K key, V oldValue) {
    Objects.requireNonNull(key);
    Objects.requireNonNull(oldValue);
    return false;
  }

  @Override
  public V getAndRemove(K key) {
    Objects.requireNonNull(key);
    return null;
  }

  @Override
  public boolean replace(K key, V oldValue, V newValue) {
    Objects.requireNonNull(key);
    Objects.requireNonNull(oldValue);
    Objects.requireNonNull(newValue);
    return false;
  }

  @Override
  public boolean replace(K key, V value) {
    Objects.requireNonNull(key);
    Objects.requireNonNull(value);
    return false;
  }

  @Override
  public V getAndReplace(K key, V value) {
    Objects.requireNonNull(key);
    Objects.requireNonNull(value);
    return null;
  }

  @Override
  public void removeAll(Set<? extends K> keys) {
    keys.forEach(Objects::requireNonNull);
  }

  @Override
  public void removeAll() {
  }

  @Override
  public void clear() {
  }

  @Override
  public <C extends Configuration<K, V>> C getConfiguration(Class<C> clazz) {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> T invoke(K key, EntryProcessor<K, V, T> entryProcessor, Object... arguments) throws EntryProcessorException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> Map<K, EntryProcessorResult<T>> invokeAll(Set<? extends K> keys, EntryProcessor<K, V, T> entryProcessor, Object... arguments) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getName() {
    throw new UnsupportedOperationException();
  }

  @Override
  public CacheManager getCacheManager() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() {
  }

  @Override
  public boolean isClosed() {
    return false;
  }

  @Override
  public <T> T unwrap(Class<T> clazz) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void registerCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void deregisterCacheEntryListener(CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator<Entry<K, V>> iterator() {
    return Collections.<Entry<K, V>>emptySet().iterator();
  }

}
