package org.ambraproject.wombat.service.remote;

import com.google.common.base.Preconditions;
import org.ambraproject.wombat.util.CacheParams;
import org.ambraproject.wombat.util.UrlParamBuilder;

import java.util.Objects;
import java.util.OptionalInt;
import java.util.UUID;

public abstract class ContentKey {

  protected final String key;

  // Nested subclasses only
  private ContentKey(String key) {
    Preconditions.checkArgument(!key.isEmpty());
    this.key = key;
  }

  public abstract void setParameters(UrlParamBuilder requestParams);

  public abstract String asCacheKey();

  @Override
  public boolean equals(Object o) {
    return this == o || o != null && getClass() == o.getClass() && key.equals(((ContentKey) o).key);
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }


  public static ContentKey createForLatestVersion(String key) {
    return new VersionContentKey(key, OptionalInt.empty());
  }

  public static ContentKey createForVersion(String key, int version) {
    return new VersionContentKey(key, OptionalInt.of(version));
  }

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


  public static ContentKey createForUuid(String key, UUID uuid) {
    return new UuidContentKey(key, uuid);
  }

  private static final class UuidContentKey extends ContentKey {
    private final UUID uuid;

    public UuidContentKey(String key, UUID uuid) {
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
