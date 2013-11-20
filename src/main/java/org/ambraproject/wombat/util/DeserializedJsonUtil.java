package org.ambraproject.wombat.util;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
   * <p/>
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
  public static Object readField(Object jsonObject, String... fieldNames) {
    Preconditions.checkNotNull(jsonObject);
    for (int i = 0; i < fieldNames.length; i++) {
      if (!(jsonObject instanceof Map)) {
        List<String> path = Arrays.asList(fieldNames).subList(0, i);
        String valueDescription = path.isEmpty() ? "Argument" : "Field at " + path;
        String message = String.format("%s is not a Map (%s)",
            valueDescription, jsonObject.getClass().getName());
        throw new IllegalArgumentException(message);
      }
      jsonObject = ((Map<?, ?>) jsonObject).get(fieldNames[i]);

      if (jsonObject == null) {
        List<String> path = Arrays.asList(fieldNames).subList(0, i + 1);
        throw new IllegalArgumentException("Field not found at " + path);
      }
    }
    return jsonObject;
  }

}
