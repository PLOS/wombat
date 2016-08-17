package org.ambraproject.wombat.model;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.ambraproject.wombat.config.theme.Theme;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ArticleType {

  private final String name;
  private final String pluralName;
  private final String code;
  private final String description;

  private ArticleType(String name, String pluralName, String code, String description) {
    this.name = Objects.requireNonNull(name);
    this.pluralName = Objects.requireNonNull(pluralName);
    this.code = code;
    this.description = Strings.nullToEmpty(description);
  }

  public String getName() {
    return name;
  }

  public String getPluralName() {
    return pluralName;
  }

  public Optional<String> getCode() {
    return Optional.ofNullable(code);
  }

  public String getDescription() {
    return description;
  }


  public static ImmutableList<ArticleType> read(Theme theme) throws IOException {
    Map<String, ?> articleTypeMap = theme.getConfigMap("articleType");
    List<Map<String, ?>> articleTypeList = (List<Map<String, ?>>) articleTypeMap.get("types");
    Collection<ArticleType> articleTypes = articleTypeList.stream()
        .map((Map<String, ?> articleType) -> {
          String name = (String) articleType.get("name");
          String pluralName = (String) articleType.get("plural");
          String code = (String) articleType.get("code");
          String description = (String) articleType.get("description");
          return new ArticleType(name, pluralName, code, description);
        })
        .collect(Collectors.toList());
    return ImmutableList.copyOf(articleTypes);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ArticleType that = (ArticleType) o;
    if (!name.equals(that.name)) return false;
    if (!pluralName.equals(that.pluralName)) return false;
    if (code != null ? !code.equals(that.code) : that.code != null) return false;
    return description.equals(that.description);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + pluralName.hashCode();
    result = 31 * result + (code != null ? code.hashCode() : 0);
    result = 31 * result + description.hashCode();
    return result;
  }
}
