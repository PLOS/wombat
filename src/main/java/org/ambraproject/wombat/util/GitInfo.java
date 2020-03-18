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

package org.ambraproject.wombat.util;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.util.Properties;

/**
 * Information about git commit
 */
public class GitInfo {
  private static final Logger log = LogManager.getLogger(GitInfo.class);

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
      log.warn("Could not get the git commit info", e);
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
