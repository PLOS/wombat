package org.ambraproject.wombat.model;

import com.google.common.collect.ImmutableList;
import org.ambraproject.wombat.config.theme.Theme;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ArticleType {

  private final String name;
  private final String pluralName;
  private final String code;
  private final String description;

  private ArticleType(String name, String pluralName, String code, String description) {
    this.name = Objects.requireNonNull(name);
    this.pluralName = pluralName;
    this.code = code;
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public String getPluralName() {
    return pluralName;
  }

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }


  public static final ArticleType UNCLASSIFIED = new ArticleType("unclassified", null, null, null);

  public static ImmutableList<ArticleType> read(Theme theme) {
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
    if (pluralName != null ? !pluralName.equals(that.pluralName) : that.pluralName != null) return false;
    if (code != null ? !code.equals(that.code) : that.code != null) return false;
    return description != null ? description.equals(that.description) : that.description == null;
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + (pluralName != null ? pluralName.hashCode() : 0);
    result = 31 * result + (code != null ? code.hashCode() : 0);
    result = 31 * result + (description != null ? description.hashCode() : 0);
    return result;
  }
}
