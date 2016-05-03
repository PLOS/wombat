package org.ambraproject.wombat.service.remote;

import com.google.common.base.Preconditions;
import org.ambraproject.wombat.util.CacheParams;
import org.ambraproject.wombat.util.UrlParamBuilder;

import java.util.Objects;
import java.util.OptionalInt;
import java.util.UUID;

/**
 * Uniquely identifies an entity in the Content Repo, using various ways to distinguish versions.
 */
public abstract class ContentKey {

  protected final String key;

  // Nested subclasses only
  private ContentKey(String key) {
    Preconditions.checkArgument(!key.isEmpty());
    this.key = key;
  }

  /**
   * Set the parameters for a Content Repo request to identify this key's entity.
   *
   * @param requestParams the builder to modify by adding parameters
   */
  public abstract void setParameters(UrlParamBuilder requestParams);

  /**
   * @return a cache key that is reliably unique among all {@link ContentKey} instances
   */
  public abstract String asCacheKey();

  @Override
  public boolean equals(Object o) {
    return this == o || o != null && getClass() == o.getClass() && key.equals(((ContentKey) o).key);
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }


  /**
   * @param key a Content Repo key
   * @return a pointer at the latest version with that key
   */
  public static ContentKey createForLatestVersion(String key) {
    return new VersionContentKey(key, OptionalInt.empty());
  }

  /**
   * @param key     a Content Repo key
   * @param version a Content Repo version number
   * @return a pointer at the identified version
   */
  public static ContentKey createForVersion(String key, int version) {
    return new VersionContentKey(key, OptionalInt.of(version));
  }

  /**
   * Identifies a Content Repo entity with the progression of version numbers.
   */
  private static final class VersionContentKey extends ContentKey {
    private final OptionalInt version;

    private VersionContentKey(String key, OptionalInt version) {
      super(key);
      Preconditions.checkArgument(!version.isPresent() || version.getAsInt() >= 0);
      this.version = version;
    }

    @Override
    public void setParameters(UrlParamBuilder requestParams) {
      requestParams.add("key", key);
      if (version.isPresent()) {
        requestParams.add("version", Integer.toString(version.getAsInt()));
      }
    }

    @Override
    public String asCacheKey() {
      String versionString = version.isPresent() ? Integer.toString(version.getAsInt()) : String.valueOf((Object) null);
      return CacheParams.createKeyHash(key, versionString);
    }

    @Override
    public String toString() {
      return String.format("[key: %s, version: %s]", key, version);
    }

    @Override
    public boolean equals(Object o) {
      return this == o || super.equals(o) && version.equals(((VersionContentKey) o).version);
    }

    @Override
    public int hashCode() {
      return 31 * super.hashCode() + version.hashCode();
    }
  }


  /**
   * @param key  a Content Repo key
   * @param uuid a UUID identifying a Content Repo version
   * @return a pointer at the identified version
   */
  public static ContentKey createForUuid(String key, UUID uuid) {
    return new UuidContentKey(key, uuid);
  }

  /**
   * Identifies a Content Repo entity with a UUID pointing directly a a version.
   */
  private static final class UuidContentKey extends ContentKey {
    private final UUID uuid;

    private UuidContentKey(String key, UUID uuid) {
      super(key);
      this.uuid = Objects.requireNonNull(uuid);
    }

    @Override
    public void setParameters(UrlParamBuilder requestParams) {
      requestParams.add("key", key).add("uuid", uuid.toString());
    }

    @Override
    public String asCacheKey() {
      return CacheParams.createKeyHash(key, uuid.toString());
    }

    @Override
    public String toString() {
      return String.format("[key: %s, uuid: %s]", key, uuid);
    }

    @Override
    public boolean equals(Object o) {
      return this == o || super.equals(o) && uuid.equals(((UuidContentKey) o).uuid);
    }

    @Override
    public int hashCode() {
      return 31 * super.hashCode() + uuid.hashCode();
    }
  }

}
