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

package org.ambraproject.wombat.config;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.config.theme.ThemeTree;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Instance of {@link RuntimeConfiguration} suitable for tests.  Many of the return values here will be null or
 * meaningless defaults; we can fill them in with "real" values as tests need them.
 */
public class TestRuntimeConfiguration implements RuntimeConfiguration {

  private ImmutableMap<String, Theme> themeMap;

  private ThemeTree themeTree;

  @Override
  public String getCompiledAssetDir() {
    return System.getProperty("java.io.tmpdir");
  }

  @Override
  public URL getServer() {
    return null;
  }

  @Override
  public Optional<URL> getSolrServer() {
    return Optional.empty();
  }

  @Override
  public String getMailServer() {
    return null;
  }

  @Override
  public String getRootPagePath() {
    return null;
  }

  @Override
  public ImmutableSet<String> getEnabledDevFeatures() { return ImmutableSet.of(); }

  @Override
  public ThemeTree getThemes(Collection<? extends Theme> internalThemes, Theme rootTheme) throws ThemeTree.ThemeConfigurationException {
    Map<String, Theme> mutable = new HashMap<>();
    mutable.put("root", rootTheme);
    for (Theme theme : internalThemes) {
      if (!theme.equals(rootTheme)) {
        mutable.put(theme.getKey(), theme);
      }
    }
    themeMap = ImmutableMap.copyOf(mutable);
    themeTree = new ThemeTree(themeMap);
    return themeTree;
  }

  @Override
  public SiteSet getSites(ThemeTree themeTree) {
    List<Map<String, ?>> spec = new ArrayList<>();
    for (Theme theme : themeTree.getThemes()) {
      if (!"root".equals(theme.getKey())) {
        Map<String, String> map = new HashMap<>();
        map.put("key", theme.getKey());
        map.put("theme", theme.getKey());
        spec.add(map);
      }
    }
    return SiteSet.create(spec, themeTree);
  }

  @Override
  public CacheConfiguration getCacheConfiguration() {
    return new CacheConfiguration() {
      @Override
      public String getMemcachedHost() {
        return null;
      }

      @Override
      public int getMemcachedPort() {
        return -1;
      }

      @Override
      public String getCacheAppPrefix() {
        return "testWombat";
      }
    };
  }

  @Override
  public HttpConnectionPoolConfiguration getHttpConnectionPoolConfiguration() {
    return new HttpConnectionPoolConfiguration() {
      @Override
      public Integer getMaxTotal() {
        return null;
      }

      @Override
      public Integer getDefaultMaxPerRoute() {
        return null;
      }
    };
  }

  @Override
  public Optional<CasConfiguration> getCasConfiguration() {
    return Optional.empty();
  }

  @Override
  public boolean areCommentsDisabled() {
    return false;
  }
}
