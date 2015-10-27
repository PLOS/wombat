package org.ambraproject.wombat.util;

import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.RandomAccess;

/**
 * Utilities for accessing objects deserialized from JSON.
 */
public class DeserializedJsonUtil {
  private DeserializedJsonUtil() {
    throw new AssertionError("Not instantiable");
  }

  /**
   * Read a nested field from a JSON object serialized to a {@link Map}. If more than one field name is provided, read
   * from nested maps.
   * <p>
   * For example, if {@code jsonObject} is a Map deserialized from
   * <pre>
   *   {
   *     "foo": {
   *       "bar": 1,
   *       "baz": 2
   *     }
   *   }
   * </pre>
   * then {@code readField(jsonObject, "foo", "bar")} would return {@code 1}.
   *
   * @param jsonObject a JSON object deserialized to a {@link Map} or nested Maps
   * @param fieldNames a sequence of field names
   * @return the value
   */
  public static Object readField(Object jsonObject, List<String> fieldNames) {
    Objects.requireNonNull(jsonObject);
    if (!(fieldNames instanceof RandomAccess)) {
      fieldNames = ImmutableList.copyOf(fieldNames);
    }

    for (int i = 0; i < fieldNames.size(); i++) {
      String fieldName = Objects.requireNonNull(fieldNames.get(i));

      if (!(jsonObject instanceof Map)) {
        throw new IllegalArgumentException(String.format("%s is not a Map (%s)",
            (i == 0 ? "Argument" : "Field at " + fieldNames.subList(0, i)), jsonObject.getClass().getName()));
      }
      jsonObject = ((Map<?, ?>) jsonObject).get(fieldName);

      if (jsonObject == null) {
        return null;
      }
    }
    return jsonObject;
  }

  public static Object readField(Object jsonObject, String... fieldNames) {
    return readField(jsonObject, Arrays.asList(fieldNames));
  }

}
