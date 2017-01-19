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

package org.ambraproject.wombat.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.ambraproject.wombat.config.theme.Theme;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ArticleType {

  private final String name;
  private final String pluralName;
  private final String code;
  private final String description;

  private ArticleType(String name) {
    this(name, null, null, null);
  }

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


  private static List<ArticleType> read(Theme theme) {
    Map<String, ?> articleTypeMap = theme.getConfigMap("articleType");

    List<Map<String, ?>> articleTypeList = (List<Map<String, ?>>) articleTypeMap.get("types");
    return articleTypeList.stream()
        .map((Map<String, ?> articleType) -> {
          String name = (String) articleType.get("name");
          String pluralName = (String) articleType.get("plural");
          String code = (String) articleType.get("code");
          String description = (String) articleType.get("description");
          return new ArticleType(name, pluralName, code, description);
        })
        .collect(Collectors.toList());
  }

  public static Dictionary getDictionary(Theme theme) {
    return new Dictionary(read(theme));
  }

  /**
   * Stores the configured article types for a theme.
   */
  public static class Dictionary {
    private final ImmutableMap<String, ArticleType> map;

    private Dictionary(List<ArticleType> types) {
      this.map = Maps.uniqueIndex(types, ArticleType::getName);
    }

    /**
     * @return the list of article types supported by this object's theme, in the order in which they should appear
     */
    public ImmutableList<ArticleType> getSequence() {
      return map.values().asList();
    }

    private static final ArticleType UNCLASSIFIED = new ArticleType("Unclassified");

    /**
     * Look up an article type according to this object's theme. If the name is not configured for this theme, provide a
     * one-off type with that name and no other configured values. For null article type names, return an object
     * representing unclassified articles.
     *
     * @param name the article type name (nullable)
     * @return the configured article type if it available, or a non-null default otherwise
     */
    public ArticleType lookUp(String name) {
      if (name == null) return UNCLASSIFIED;
      ArticleType type = map.get(name);
      return (type != null) ? type : new ArticleType(name);
    }
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
