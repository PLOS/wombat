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

import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import org.ambraproject.wombat.cache.Cache;
import org.ambraproject.wombat.cache.NullCache;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.theme.TestClasspathTheme;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.config.theme.ThemeGraph;
import org.ambraproject.wombat.service.AssetService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Defines spring beans needed by tests.
 */
@Configuration
public class TestSpringConfiguration {

  @Bean
  public RuntimeConfiguration runtimeConfiguration() {
    return new TestRuntimeConfiguration();
  }

  @Bean
  public ThemeGraph themeGraph()
      throws ThemeGraph.ThemeConfigurationException {
    TestClasspathTheme rootTheme = new TestClasspathTheme("root", ImmutableList.of());
    TestClasspathTheme theme1 = new TestClasspathTheme("site1", ImmutableList.of(rootTheme));
    TestClasspathTheme theme2 = new TestClasspathTheme("site2", ImmutableList.of(rootTheme));
    Set<Theme> themes = ImmutableSet.of(rootTheme, theme1, theme2);
    return new ThemeGraph(Maps.uniqueIndex(themes, Theme::getKey));
  }

  @Bean
  public SiteSet siteSet(ThemeGraph themeGraph) {
    List<ImmutableMap<String, Object>> siteSpecifications = themeGraph.getThemes().stream()
        .filter((Theme theme) -> !theme.getKey().equals("root"))
        .map((Theme theme) -> {
          String key = theme.getKey();
          return ImmutableMap.<String, Object>builder()
              .put("key", key) // reuse theme key as site key
              .put("theme", key) // specify that the site to be built will use this theme
              .build();
        })
        .collect(Collectors.toList());
    return SiteSet.create(siteSpecifications, themeGraph);
  }

  @Bean
  public AssetService assetService() {
    return mock(AssetService.class);
  }

  @Bean
  public Cache cache() {
    return new NullCache();
  }
}
