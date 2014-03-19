package org.ambraproject.wombat.util;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

/**
 * An object encapsulating properties of a build of an application (either this one, or the service component).
 *
 * @see org.ambraproject.wombat.service.BuildInfoService
 */
public class BuildInfo {

  private final String version;
  private final String date;
  private final String user;

  public BuildInfo(String version, String date, String user) {
    this.version = Strings.nullToEmpty(version);
    this.date = Strings.nullToEmpty(date);
    this.user = Strings.nullToEmpty(user);
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

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("version", version)
        .add("date", date)
        .add("user", user)
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

    return true;
  }

  @Override
  public int hashCode() {
    int result = version.hashCode();
    result = 31 * result + date.hashCode();
    result = 31 * result + user.hashCode();
    return result;
  }
}
