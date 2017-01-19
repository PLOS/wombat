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

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.List;

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
    return Objects.toStringHelper(this)
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
