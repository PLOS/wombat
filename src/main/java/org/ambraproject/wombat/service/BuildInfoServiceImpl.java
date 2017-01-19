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

package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.util.BuildInfo;
import org.ambraproject.wombat.util.GitInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class BuildInfoServiceImpl implements BuildInfoService {
  private static final Logger log = LoggerFactory.getLogger(BuildInfoServiceImpl.class);

  @Autowired
  private ArticleApi articleApi;

  @Autowired
  private GitInfo gitInfo;

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  /*
   * Cache the results in the service object. This may not be the best place to cache them, especially if the data is
   * unstable, but we can broadly assume both values are constant. We expect serviceBuildInfo to change rarely (i.e.,
   * only when the service component is upgraded), and often at the same time that this application is restarted.
   * And webappBuildInfo actually is constant.
   *
   * If these values need to not be cached here, they could be extracted out as Spring beans, so that this service
   * class is called once to initialize the beans. There are more sophisticated options for caching with eviction if we
   * want to handle the case of stale serviceBuildInfo, but that doesn't seem worthwhile right now.
   */
  private BuildInfo webappBuildInfo = null;
  private BuildInfo serviceBuildInfo = null;

  @Override
  public BuildInfo getWebappBuildInfo() {
    if (webappBuildInfo != null) {
      return webappBuildInfo;
    }
    try {
      return webappBuildInfo = fetchWebappBuildInfo();
    } catch (Exception e) {
      log.error("Could not fetch webapp build info", e);
      return null;
    }
  }

  /**
   * {@inheritDoc}
   * <p/>
   * Each service bean makes only one remote call during its lifecycle (or maybe a small number in edge cases with
   * concurrency).
   */
  @Override
  public BuildInfo getServiceBuildInfo() {
    if (serviceBuildInfo != null) {
      return serviceBuildInfo;
    }
    try {
      return serviceBuildInfo = fetchServiceBuildInfo();
    } catch (Exception e) {
      log.error("Could not fetch service build info", e);
      return null;
    }
  }

  private BuildInfo fetchWebappBuildInfo() throws IOException {
    Map<Object, Object> buildInfo = new LinkedHashMap<>();

    Properties properties = new Properties();
    try (InputStream versionStream = getClass().getResourceAsStream("/version.properties")) {
      properties.load(versionStream);
    }
    buildInfo.putAll(properties);

    buildInfo.put("gitCommitIdAbbrev", gitInfo.getCommitIdAbbrev());
    buildInfo.put("enabledDevFeatures", runtimeConfiguration.getEnabledDevFeatures());
    return parse(buildInfo);
  }

  private static final ApiAddress BUILD_CONFIG_ADDRESS = ApiAddress.builder("config").addParameter("type", "build").build();

  private BuildInfo fetchServiceBuildInfo() throws IOException {
    return parse(articleApi.requestObject(BUILD_CONFIG_ADDRESS, Map.class));
  }

  private static BuildInfo parse(Map<?, ?> propertyMap) {
    return new BuildInfo(
        (String) propertyMap.get("version"),
        (String) propertyMap.get("buildDate"),
        (String) propertyMap.get("buildUser"),
        (String) propertyMap.get("gitCommitIdAbbrev"),
        (Collection<String>) propertyMap.get("enabledDevFeatures"));
  }

}
