package org.ambraproject.wombat.config;

import com.google.common.base.Preconditions;
import freemarker.cache.TemplateLoader;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads templates from a delegate {@link TemplateLoader} based on a key given in the view name.
 * <p/>
 * An instance of this class interprets the first slash-separated component of any view name passed to it as a key value
 * that indicates how to build or choose a {@code TemplateLoader} to pass the rest to. How the key value is interpreted
 * is defined by concrete subclasses. For example, if a controller returns the view name {@code "foo/bar/baz.ftl"}, the
 * subclass would take {@code "foo"} as the delegation key, use it to return some other {@code TemplateLoader} instance,
 * and pass to that instance the view name {@code "bar/baz.ftl"}.
 */
public abstract class DelegatingTemplateLoader implements TemplateLoader {

  /**
   * Interpret a view key for forwarding.
   *
   * @param key the non-null, non-empty key containing no slashes
   * @return a {@code TemplateLoader} instance to which to forward
   * @throws IllegalArgumentException if the key cannot be matched to a delegate object
   */
  protected abstract TemplateLoader delegate(String key);


  private class TemplateSource {
    private final TemplateLoader delegateLoader;
    private final Object delegateTemplateSource;

    private TemplateSource(TemplateLoader delegateLoader, Object delegateTemplateSource) {
      this.delegateLoader = Preconditions.checkNotNull(delegateLoader);
      this.delegateTemplateSource = Preconditions.checkNotNull(delegateTemplateSource);
    }
  }

  /**
   * Delegate to a loader and get a template source to it. The view name contains both the forwarding key (before the
   * first slash) and the view name to pass to the delegate loader (after the first slash).
   *
   * @param viewName the view name
   * @return an object containing the loader indicated by the key and the source indicated by the view name
   * @throws IOException
   */
  private TemplateSource buildTemplateSource(String viewName) throws IOException {
    int sep = viewName.indexOf('/');
    Preconditions.checkArgument(sep >= 0);

    String key = viewName.substring(0, sep);
    TemplateLoader delegateLoader = delegate(key);
    if (delegateLoader == null) {
      throw new IllegalArgumentException();
    }

    String delegateViewName = viewName.substring(sep + 1);
    Object delegateTemplateSource = findTemplateSource(delegateLoader, delegateViewName);

    return new TemplateSource(delegateLoader, delegateTemplateSource);
  }

  private static final Pattern LOCALIZED_FTL_NAME = Pattern.compile("(.*)_[^.]*?(\\..*?)");

  /**
   * Chop localization extensions from the name until we get a hit. For example, for {@code "home_en_US.ftl"}, try to
   * load {@code "home_en_US.ftl"}, then {@code "home_en.ftl"}, then {@code "home.ftl"}. This hackishly replicates some
   * logic from {@link freemarker.cache.TemplateCache}{@code .findTemplateSource()}.
   *
   * @param delegateLoader
   * @param viewName
   * @return
   * @throws IOException
   */
  private static Object findTemplateSource(TemplateLoader delegateLoader, String viewName) throws IOException {
    while (true) {
      Object source = delegateLoader.findTemplateSource(viewName);
      if (source != null) {
        return source;
      }

      Matcher matcher = LOCALIZED_FTL_NAME.matcher(viewName);
      if (!matcher.matches()) {
        throw new IllegalArgumentException(viewName);
      }
      viewName = matcher.group(1) + matcher.group(2);
    }
  }


  @Override
  public Object findTemplateSource(String name) throws IOException {
    return buildTemplateSource(name);
  }

  @Override
  public long getLastModified(Object templateSource) {
    TemplateSource ts = (TemplateSource) templateSource;
    return ts.delegateLoader.getLastModified(ts.delegateTemplateSource);
  }

  @Override
  public Reader getReader(Object templateSource, String encoding) throws IOException {
    TemplateSource ts = (TemplateSource) templateSource;
    return ts.delegateLoader.getReader(ts.delegateTemplateSource, encoding);
  }

  @Override
  public void closeTemplateSource(Object templateSource) throws IOException {
    TemplateSource ts = (TemplateSource) templateSource;
    ts.delegateLoader.closeTemplateSource(ts.delegateTemplateSource);
  }

}
