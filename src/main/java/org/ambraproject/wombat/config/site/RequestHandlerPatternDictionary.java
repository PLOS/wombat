package org.ambraproject.wombat.config.site;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableTable;
import org.springframework.web.bind.annotation.RequestMapping;

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
public final class RequestHandlerPatternDictionary {

  private final ImmutableTable.Builder<String, Site, String> registry;

  // While null, this object is in a state to accept writes. Initialized on first call to getPattern.
  // By convention, never assign to this field (i.e., treat as final) if it is not null.
  private ImmutableTable<String, Site, String> table;

  public RequestHandlerPatternDictionary() {
    registry = ImmutableTable.builder();
    table = null;
  }

  /**
   * Register the pattern that is associated with a handler on a particular site.
   * <p>
   * All registrations must be completed before the first call to {@link #getPattern}.
   *
   * @param handlerAnnotation the annotation for the handler
   * @param site              the site associated with the handler
   * @param pattern           the pattern to register
   * @throws IllegalStateException if {@link #getPattern} has been called once or more on this object
   */
  public void register(RequestMapping handlerAnnotation, Site site, String pattern) {
    String handlerName = Preconditions.checkNotNull(handlerAnnotation.name());
    Preconditions.checkNotNull(site);
    Preconditions.checkNotNull(pattern);

    if (table != null) {
      throw new IllegalStateException("Cannot register more methods after directory has been read");
    }
    synchronized (registry) {
      registry.put(handlerName, site, pattern);
    }
  }

  /**
   * Look up a registered pattern.
   * <p>
   * This method should only be called when all registrations are complete. The first time this method is called for
   * this object, it has the side effect of freezing the object and invalidating any future calls to {@link #register}.
   *
   * @param handlerName the name of the handler
   * @param site        the site associated with the handler
   * @return the pattern, or {@code null} if no handler exists for the given name on the given site
   */
  public String getPattern(String handlerName, Site site) {
    /*
     * We permit a harmless race condition here: if the first two calls to this method happen concurrently, we might
     * build the registry more than once.
     *
     * There are some other, weird race conditions that can happen if the first call to this method happens
     * concurrently with a call to the register method. We expect the last register call to happen synchronously before
     * the first getPattern call, so it is a bug if there is any chance of it happening the other way, and we would
     * like to throw an IllegalStateException if possible. In the event of a bug that causes the two events to be
     * ordered non-deterministically (even if we synchronize in this method), we throw IllegalStateException on a
     * best-effort basis, but can't guarantee it.
     */
    if (table == null) {
      table = registry.build();
    }
    return table.get(Preconditions.checkNotNull(handlerName), Preconditions.checkNotNull(site));
  }

}
