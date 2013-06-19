package org.ambraproject.wombat.config;

import com.google.common.collect.ImmutableList;
import freemarker.cache.TemplateLoader;

import java.io.IOException;

public abstract class Theme {

  private final String key;
  private final ImmutableList<Theme> chain;

  protected Theme(String key, Theme parent) {
    this.key = key;
    this.chain = (parent == null)
        ? ImmutableList.of(this)
        : ImmutableList.<Theme>builder().add(this).addAll(parent.chain).build();
  }

  /**
   * Get the theme's key. This is the name assigned in the webapp configuration.
   *
   * @return the key
   */
  public String getKey() {
    return key;
  }

  /**
   * Return the inheritance chain of themes, from leaf to root. The first element is guaranteed to be this object. If
   * this is a root theme (no parent), the list's size is 1. Else, the second element (if any) is this element's parent
   * theme, and each element is followed by its parent until a root theme is reached.
   * <p/>
   * The returned list is guaranteed to be non-null and non-empty.
   *
   * @return the chain of themes
   */
  public final ImmutableList<Theme> getChain() {
    return chain;
  }

  public final TemplateLoader[] getTemplateLoaderArray() throws IOException {
    TemplateLoader[] loaders = new TemplateLoader[chain.size()];
    int i = 0;
    for (Theme theme : getChain()) {
      loaders[i++] = theme.getTemplateLoader();
    }
    assert i == loaders.length;
    return loaders;
  }

  public abstract TemplateLoader getTemplateLoader() throws IOException;

  /*
   * TODO: Add public abstract method that accesses theme's static content (e.g., HTML, CSS, JavaScript)
   */

}
