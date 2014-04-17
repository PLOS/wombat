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

/**
 * Information about git commit
 */
public class GitInfoService {
  String branch;
  String describe;
  String commitId;
  String commitIdAbbrev;
  String commitUserName;
  String commitUserEmail;
  String commitMessageFull;
  String commitMessageShort;
  String commitTime;
  String buildUserName;
  String buildUserEmail;
  String buildTime;

  public GitInfoService() {

  }

  public String getBranch() {
    return branch;
  }

  public void setBranch(String branch) {
    this.branch = branch;
  }

  public String getDescribe() {
    return describe;
  }

  public void setDescribe(String describe) {
    this.describe = describe;
  }

  public String getCommitId() {
    return commitId;
  }

  public void setCommitId(String commitId) {
    this.commitId = commitId;
  }

  public String getCommitIdAbbrev() {
    return commitIdAbbrev;
  }

  public void setCommitIdAbbrev(String commitIdAbbrev) {
    this.commitIdAbbrev = commitIdAbbrev;
  }

  public String getCommitUserName() {
    return commitUserName;
  }

  public void setCommitUserName(String commitUserName) {
    this.commitUserName = commitUserName;
  }

  public String getCommitUserEmail() {
    return commitUserEmail;
  }

  public void setCommitUserEmail(String commitUserEmail) {
    this.commitUserEmail = commitUserEmail;
  }

  public String getCommitMessageFull() {
    return commitMessageFull;
  }

  public void setCommitMessageFull(String commitMessageFull) {
    this.commitMessageFull = commitMessageFull;
  }

  public String getCommitMessageShort() {
    return commitMessageShort;
  }

  public void setCommitMessageShort(String commitMessageShort) {
    this.commitMessageShort = commitMessageShort;
  }

  public String getCommitTime() {
    return commitTime;
  }

  public void setCommitTime(String commitTime) {
    this.commitTime = commitTime;
  }

  public String getBuildUserName() {
    return buildUserName;
  }

  public void setBuildUserName(String buildUserName) {
    this.buildUserName = buildUserName;
  }

  public String getBuildUserEmail() {
    return buildUserEmail;
  }

  public void setBuildUserEmail(String buildUserEmail) {
    this.buildUserEmail = buildUserEmail;
  }

  public String getBuildTime() {
    return buildTime;
  }

  public void setBuildTime(String buildTime) {
    this.buildTime = buildTime;
  }
}
