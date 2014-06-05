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

package org.ambraproject.wombat.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.util.Properties;

/**
 * Information about git commit
 */
public class GitInfo {
  private static final Logger log = LoggerFactory.getLogger(GitInfo.class);

  private final String branch;
  private final String describe;
  private final String commitId;
  private final String commitIdAbbrev;
  private final String commitUserName;
  private final String commitUserEmail;
  private final String commitMessageFull;
  private final String commitMessageShort;
  private final String commitTime;
  private final String buildUserName;
  private final String buildUserEmail;
  private final String buildTime;

  public GitInfo() {
    Resource resource = new ClassPathResource("/git.properties");
    Properties props = new Properties();
    try {
      props = PropertiesLoaderUtils.loadProperties(resource);
    } catch (Exception e) {
      log.error("Could not get the git commit info", e);
    }

    this.branch = getPropertyValue(props, "git.branch");

    this.describe = getPropertyValue(props, "git.commit.id.describe");
    this.commitId = getPropertyValue(props, "git.commit.id");
    this.commitIdAbbrev = getPropertyValue(props, "git.commit.id.abbrev");
    this.commitUserName = getPropertyValue(props, "git.commit.user.name");
    this.commitUserEmail = getPropertyValue(props, "git.commit.user.email");
    this.commitMessageFull = getPropertyValue(props, "git.commit.message.full");
    this.commitMessageShort = getPropertyValue(props, "git.commit.message.short");
    this.commitTime = getPropertyValue(props, "git.commit.time");

    this.buildUserName = getPropertyValue(props, "git.build.user.name");
    this.buildUserEmail = getPropertyValue(props, "git.build.user.email");
    this.buildTime = getPropertyValue(props, "git.build.time");
  }

  /**
   * This is to handle a situation where git folder isn't available
   * @param properties Properties object that contains the git.properties info
   * @param key key
   * @return value, default value is an empty string
   */
  private String getPropertyValue(Properties properties, String key) {
    String value = properties.getProperty(key);
    if (value != null && !value.startsWith("${")) {
      return value;
    } else {
      return "";
    }
  }

  public String getBranch() {
    return branch;
  }

  public String getDescribe() {
    return describe;
  }

  public String getCommitId() {
    return commitId;
  }

  public String getCommitIdAbbrev() {
    return commitIdAbbrev;
  }

  public String getCommitUserName() {
    return commitUserName;
  }

  public String getCommitUserEmail() {
    return commitUserEmail;
  }

  public String getCommitMessageFull() {
    return commitMessageFull;
  }

  public String getCommitMessageShort() {
    return commitMessageShort;
  }

  public String getCommitTime() {
    return commitTime;
  }

  public String getBuildUserName() {
    return buildUserName;
  }

  public String getBuildUserEmail() {
    return buildUserEmail;
  }

  public String getBuildTime() {
    return buildTime;
  }
}
