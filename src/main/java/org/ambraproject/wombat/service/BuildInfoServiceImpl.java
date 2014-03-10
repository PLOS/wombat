package org.ambraproject.wombat.service;

import org.ambraproject.wombat.util.BuildInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class BuildInfoServiceImpl implements BuildInfoService {

  @Autowired
  private SoaService soaService;

  private BuildInfo localBuildInfo = null;
  private BuildInfo serviceBuildInfo = null;

  @Override
  public BuildInfo getLocalBuildInfo() {
    if (localBuildInfo != null) {
      return localBuildInfo;
    }
    try {
      return localBuildInfo = fetchLocalBuildInfo();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public BuildInfo getServiceBuildInfo() {
    if (serviceBuildInfo != null) {
      return serviceBuildInfo;
    }
    try {
      return serviceBuildInfo = fetchServiceBuildInfo();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private BuildInfo fetchLocalBuildInfo() throws IOException {
    Properties properties = new Properties();
    try (InputStream versionStream = getClass().getResourceAsStream("/version.properties")) {
      properties.load(versionStream);
    }
    return parse(properties);
  }

  private BuildInfo fetchServiceBuildInfo() throws IOException {
    return parse(soaService.requestObject("build", Map.class));
  }

  private static BuildInfo parse(Map<?, ?> propertyMap) {
    return new BuildInfo(
        (String) propertyMap.get("version"),
        (String) propertyMap.get("buildDate"),
        (String) propertyMap.get("buildUser"));
  }

}
