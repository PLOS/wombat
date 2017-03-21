/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.ambraproject.wombat.config.site;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * A global bean that looks up patterns that have been mapped to request handlers. Ordinarily those patterns are set up
 * as part of Spring configuration; we want to capture those patterns in order to use them later, for building links.
 * <p>
 * This bean has two roles: intercepting the patterns before they get passed to Spring config (which, as far as we know,
 * doesn't have any built-in way to extract them later), and looking them up later. It would be nice if we could have
 * one bean to do the first job, which produces another, immutable bean to do the second job when it's done.
 * Unfortunately, we're not aware of a Spring hook that allows us to set up a separate bean only after all request
 * handlers have been mapped. For the next-best thing, we statefully freeze this object the first time it is read and
 * throw an {@code IllegalStateException} rather than allow any further writes, making it effectively immutable.
 * <p>
 * This class is intended to be thread-safe. Writes are synchronized, and the object is immutable while being read.
 */
public final class RequestMappingContextDictionary {
  private static final Logger log = LoggerFactory.getLogger(RequestMappingContextDictionary.class);

  // While false, this object is in a state to accept writes. Permanently set to true on first call to getPattern.
  private boolean isFrozen = false;

  private final Object writeLock = new Object();

  private final Table<String, Site, RequestMappingContext> siteTableBuilder;
  private final Map<String, RequestMappingContext> globalTableBuilder;

  // By convention, assign to each of these fields only once, when isFrozen is set to true.
  private ImmutableTable<String, Site, RequestMappingContext> siteTable = null;
  private ImmutableMap<String, RequestMappingContext> globalTable = null;

  public RequestMappingContextDictionary() {
    siteTableBuilder = HashBasedTable.create();
    globalTableBuilder = new HashMap<>();
  }

  private static String getHandlerName(RequestMappingContext mapping) {
    return Preconditions.checkNotNull(mapping.getAnnotation().name());
  }

  /**
   * Store a registered mapping and check whether there is a key collision.
   *
   * @param mapping           a mapping to be registered
   * @param insertionFunction a function that stores the mapping object in this object at a particular key and returns
   *                          the mapping that was previously at the same key, or {@code null} if none
   * @throws IllegalStateException if {@link #getPattern} has been called once or more on this object
   * @throws RuntimeException      if the key that {@code insertionFunction} uses has a collision
   */
  private void insertMapping(RequestMappingContext mapping,
                             UnaryOperator<RequestMappingContext> insertionFunction) {
    synchronized (writeLock) {
      if (isFrozen) {
        throw new IllegalStateException("Cannot register more methods after directory has been read");
      }

      RequestMappingContext previous = insertionFunction.apply(mapping);

      if (previous != null) {
        if (previous.equals(mapping)) {
          log.debug("Registered redundant mapping: {}", getHandlerName(mapping));
        } else {
          throw new RuntimeException("Key collision for mappings: " + getHandlerName(mapping));
        }
      }
    }
  }

  /**
   * Register the pattern that is associated with a handler on a particular site.
   * <p>
   * All registrations must be completed before the first call to {@link #getPattern}.
   *
   * @param mapping the mapping to the handler
   * @param site    the site associated with the handler
   * @throws IllegalStateException if {@link #getPattern} has been called once or more on this object
   */
  public void registerSiteMapping(RequestMappingContext mapping, Site site) {
    Preconditions.checkNotNull(site);
    Preconditions.checkArgument(!mapping.isSiteless());
    String handlerName = getHandlerName(mapping);

    insertMapping(mapping, m -> siteTableBuilder.put(handlerName, site, m));
  }

  /**
   * Register the pattern that is associated with a siteless handler.
   * <p>
   * All registrations must be completed before the first call to {@link #getPattern}.
   *
   * @param mapping the mapping to the handler
   * @throws IllegalStateException if {@link #getPattern} has been called once or more on this object
   */
  public void registerGlobalMapping(RequestMappingContext mapping) {
    Preconditions.checkArgument(mapping.isSiteless());
    String handlerName = getHandlerName(mapping);

    insertMapping(mapping, m -> globalTableBuilder.put(handlerName, m));
  }

  private void buildAndFreeze() {
    /*
     * We permit a harmless race condition on the `if (!isFrozen)` block: if the first two calls to this method happen
     * concurrently, we might copy the builders more than once. This is safe because they will contain identical data.
     */
    if (!isFrozen) {
      synchronized (writeLock) {
        siteTable = ImmutableTable.copyOf(siteTableBuilder);
        globalTable = ImmutableMap.copyOf(globalTableBuilder);
        isFrozen = true;
      }
    }
  }

  /**
   * Look up a registered pattern.
   * <p>
   * This method should only be called when all registrations are complete. The first time this method is called for
   * this object, it has the side effect of freezing the object and invalidating any future calls to {@link
   * #registerSiteMapping} and {@link #registerGlobalMapping}.
   *
   * @param handlerName the name of the handler
   * @param site        the site associated with the request to map. Use null here in the case of a siteless mapping
   * @return the pattern, or {@code null} if no handler exists for the given name on the given site
   */
  public RequestMappingContext getPattern(String handlerName, Site site) {
    buildAndFreeze();
    Preconditions.checkNotNull(handlerName);
    if (site != null) {
      RequestMappingContext siteMapping = siteTable.get(handlerName, site);
      if (siteMapping != null) {
        return siteMapping;
      }
    }
    return globalTable.get(handlerName);
  }


  public static interface MappingEntry {
    String getHandlerName();

    Optional<Site> getSite();

    RequestMappingContext getMapping();
  }

  public Iterable<MappingEntry> getAll() {
    buildAndFreeze();

    Iterable<MappingEntry> siteEntries = Iterables.transform(siteTable.cellSet(),
        (Table.Cell<String, Site, RequestMappingContext> cell) ->
            new MappingEntry() {
              @Override
              public String getHandlerName() {
                return cell.getRowKey();
              }

              @Override
              public Optional<Site> getSite() {
                return Optional.of(cell.getColumnKey());
              }

              @Override
              public RequestMappingContext getMapping() {
                return cell.getValue();
              }
            });

    Iterable<MappingEntry> globalEntries = Iterables.transform(globalTable.entrySet(),
        (Map.Entry<String, RequestMappingContext> entry) ->
            new MappingEntry() {
              @Override
              public String getHandlerName() {
                return entry.getKey();
              }

              @Override
              public Optional<Site> getSite() {
                return Optional.empty();
              }

              @Override
              public RequestMappingContext getMapping() {
                return entry.getValue();
              }
            });

    return Iterables.concat(siteEntries, globalEntries);
  }

}
