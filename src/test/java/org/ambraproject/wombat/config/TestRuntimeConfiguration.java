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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.ambraproject.wombat.config.theme.ThemeSource;

import java.net.URL;
import java.util.Optional;

/**
 * Instance of {@link RuntimeConfiguration} suitable for tests.  Many of the return values here will be null or
 * meaningless defaults; we can fill them in with "real" values as tests need them.
 */
public class TestRuntimeConfiguration implements RuntimeConfiguration {

  @Override
  public String getCompiledAssetDir() {
    return System.getProperty("java.io.tmpdir");
  }

  @Override
  public URL getServer() {
    return null;
  }

  @Override
  public Optional<SolrConfiguration> getSolrConfiguration() { return Optional.empty(); }

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
  public ImmutableList<ThemeSource<?>> getThemeSources() {
    return null;
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
