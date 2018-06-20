package org.ambraproject.wombat.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ambraproject.wombat.config.theme.Theme;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import freemarker.cache.TemplateLoader;

/**
 * Class to support unit tests.  The class returns <b>null</b> from all the methods,
 * so this class <b>MUST</b> be mocked, and the mocking instance must provide all
 * the necessary returns.
 */
public class ThemeTest extends Theme {

  /**
   * Create an instance of <code>ThemeTest</code>.
   *
   * @param key The site ID
   */
  public ThemeTest(String key) {
    super(key, ImmutableList.of());
  }

  /**
   * Create an instance of <code>ThemeTest</code>.
   *
   * @param key The site ID
   * @param parents The list of parent themes
   */
  public ThemeTest(String key, List<? extends Theme> parents) {
    super(key, parents);
  }

  @Override
  public Map<String, Object> getConfigMap(String path) {
    return ImmutableMap.of();
  }

  @Override
  public TemplateLoader getTemplateLoader() throws IOException {
    return null;
  }

  @Override
  protected InputStream fetchStaticResource(String path) throws IOException {
    return null;
  }

  @Override
  protected ResourceAttributes fetchResourceAttributes(String path) throws IOException {
    return null;
  }

  @Override
  protected Collection<String> fetchStaticResourcePaths(String root) throws IOException {
    return null;
  }

  @Override
  public String describeSource() {
    return null;
  }
}
