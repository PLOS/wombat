/*
 * Copyright (c) 2006-2014 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.wombat.service;

import org.ambraproject.wombat.service.remote.SoaService;
import org.ambraproject.wombat.util.BuildInfo;
import org.ambraproject.wombat.util.GitInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class BuildInfoServiceImpl implements BuildInfoService {
  private static final Logger log = LoggerFactory.getLogger(BuildInfoServiceImpl.class);

  @Autowired
  private SoaService soaService;

  @Autowired
  private GitInfo gitInfo;

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
    Properties properties = new Properties();
    try (InputStream versionStream = getClass().getResourceAsStream("/version.properties")) {
      properties.load(versionStream);
    }
    properties.setProperty("gitCommitIdAbbrev", gitInfo.getCommitIdAbbrev());
    return parse(properties);
  }

  private BuildInfo fetchServiceBuildInfo() throws IOException {
    return parse(soaService.requestObject("build", Map.class));
  }

  private static BuildInfo parse(Map<?, ?> propertyMap) {
    return new BuildInfo(
        (String) propertyMap.get("version"),
        (String) propertyMap.get("buildDate"),
        (String) propertyMap.get("buildUser"),
        (String) propertyMap.get("gitCommitIdAbbrev"));
  }

}
