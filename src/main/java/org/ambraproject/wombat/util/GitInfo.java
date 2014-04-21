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

import org.springframework.core.env.Environment;

/**
 * Information about git commit
 */
public class GitInfo {
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

  public GitInfo(Environment env) {
    this.branch = env.getProperty("git.branch");

    this.describe = env.getProperty("git.commit.id.describe");
    this.commitId = env.getProperty("git.commit.id");
    this.commitIdAbbrev = env.getProperty("git.commit.id.abbrev");
    this.commitUserName = env.getProperty("git.commit.user.name");
    this.commitUserEmail = env.getProperty("git.commit.user.email");
    this.commitMessageFull = env.getProperty("git.commit.message.full");
    this.commitMessageShort = env.getProperty("git.commit.message.short");
    this.commitTime = env.getProperty("git.commit.time");

    this.buildUserName = env.getProperty("git.build.user.name");
    this.buildUserEmail = env.getProperty("git.build.user.email");
    this.buildTime = env.getProperty("git.build.time");
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
