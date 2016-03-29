package org.ambraproject.wombat.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.joda.time.LocalDate;

import java.lang.reflect.Type;

/**
 * Adapter for Joda Time's {@link org.joda.time.LocalDate} class (not to be confused with {@link java.time.LocalDate},
 * for {@link com.google.gson.Gson} compatibility with {@link org.plos.ned_client.model.Relationship}.
 */
public enum JodaTimeLocalDateAdapter implements JsonDeserializer<LocalDate> {
  INSTANCE;

  @Override
  public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    return LocalDate.parse(json.getAsString());
  }

}
