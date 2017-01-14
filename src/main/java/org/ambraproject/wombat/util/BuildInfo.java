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

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;

/**
 * An object encapsulating properties of a build of an application (either this one, or the service component).
 *
 * @see org.ambraproject.wombat.service.BuildInfoService
 */
public class BuildInfo {

  private final String version;
  private final String date;
  private final String user;
  private final String gitCommitIdAbbrev;
  private ImmutableSet<String> enabledDevFeatures;

  public BuildInfo(String version, String date, String user, String gitCommitIdAbbrev,
      Collection<String> enabledDevFeatures) {
    this.version = Strings.nullToEmpty(version);
    this.date = Strings.nullToEmpty(date);
    this.user = Strings.nullToEmpty(user);
    this.gitCommitIdAbbrev = Strings.nullToEmpty(gitCommitIdAbbrev);
    this.enabledDevFeatures = (enabledDevFeatures == null) ? ImmutableSet.<String>of() : ImmutableSet.copyOf(enabledDevFeatures);
  }

  /**
   * @return the build's version number
   */
  public String getVersion() {
    return version;
  }

  /**
   * @return a timestamp of the build (format is unspecified)
   */
  public String getDate() {
    return date;
  }

  /**
   * @return the username of the agent that performed the build
   */
  public String getUser() {
    return user;
  }

  /**
   * @return the git commit short hash of the code
   */
  public String getGitCommitIdAbbrev() {
    return gitCommitIdAbbrev;
  }

  /**
   * @return a string describing the list of enabled dev features
   */
  public ImmutableSet<String> getEnabledDevFeatures() {
    return enabledDevFeatures;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("version", version)
        .add("date", date)
        .add("user", user)
        .add("gitCommitIdAbbrev", gitCommitIdAbbrev)
        .add("enabledDevFeatures", enabledDevFeatures)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    BuildInfo buildInfo = (BuildInfo) o;

    if (!date.equals(buildInfo.date)) return false;
    if (!user.equals(buildInfo.user)) return false;
    if (!version.equals(buildInfo.version)) return false;
    if (!gitCommitIdAbbrev.equals(buildInfo.gitCommitIdAbbrev)) return false;
    if (!enabledDevFeatures.equals(buildInfo.enabledDevFeatures)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = version.hashCode();
    result = 31 * result + date.hashCode();
    result = 31 * result + user.hashCode();
    result = 31 * result + gitCommitIdAbbrev.hashCode();
    result = 31 * result + enabledDevFeatures.hashCode();
    return result;
  }
}
