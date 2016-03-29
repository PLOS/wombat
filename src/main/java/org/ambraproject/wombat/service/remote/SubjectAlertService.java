package org.ambraproject.wombat.service.remote;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.plos.ned_client.model.IndividualComposite;
import org.plos.ned_client.model.Individualprofile;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Iterator;

/**
 * Add and remove subject alert for user in journal using individuals/{nedId}/alerts
 * resource.
 */
public class SubjectAlertService {

  private static final String ALERT_SOURCE = "Ambra";

  private static final String ALERT_FREQUENCY = "weekly";

  private static final String ALERT_NAME = "PLoSONE";

  @Autowired
  private UserApi userApi;

  private static enum RemoveResult {
    NOT_FOUND, EMPTY_AFTER_REMOVE, NOT_EMPTY_AFTER_REMOVE
  };



  /**
   * Add the subject alert for the loggedin user for the journal.
   * If the alert object exists for the journal it uses PUT individuals/{nedId}/alerts/{alertId}
   * to modify that alert object, otherwise it uses POST individuals/{nedId}/alerts
   * to create a new alert object.
   *
   * @param authId The authentication ID of the logged in user.
   * @param journalKey The journal key.
   * @param subjectName The subject to add alert for.
   * @throws IOException
   */
  public void addAlert(String authId, String journalKey, String subjectName) throws IOException {
    String nedId = userApi.getUserIdFromAuthId(authId);
    if (nedId == null) {
      throw new RuntimeException("failed to get NED ID");
    }

    JsonArray alerts = userApi.requestObject(String.format("individuals/%s/alerts", nedId), JsonArray.class);
    JsonObject alert = findMatchingAlert(alerts, journalKey);

    if (alert != null) {
      addSubjectToAlert(alert, subjectName);
    } else {
      alert = createAlertForSubject(journalKey, subjectName);
    }

    if (alert.has("id")) {
      String alertId = String.valueOf(alert.getAsJsonPrimitive("id").getAsLong());
      userApi.putObject(String.format("individuals/%s/alerts/%s", nedId, alertId), alert);
    } else {
      userApi.postObject(String.format("individuals/%s/alerts", nedId), alert);
    }
  }

  /**
   * Remove the subject alert for the loggedin user for the journal.
   * After removing the subject, if other subjects exist in the alert object for the journal,
   * then it uses PUT individuals/{nedId}/alerts/{alertId} to modify the alert object,
   * otherwise it uses DELETE individuals/{nedId}/alerts/{alertId} to remove the
   * alert object.
   *
   * @param authId The authentication ID of the logged in user.
   * @param journalKey The journal key.
   * @param subjectName The subject to remove alert for.
   * @throws IOException
   */
  public void removeAlert(String authId, String journalKey, String subjectName) throws IOException {
    String nedId = userApi.getUserIdFromAuthId(authId);
    if (nedId == null) {
      throw new RuntimeException("failed to get NED ID");
    }

    JsonArray alerts = userApi.requestObject(String.format("individuals/%s/alerts", nedId), JsonArray.class);
    JsonObject alert = findMatchingAlert(alerts, journalKey);

    if (alert == null) {
      throw new RuntimeException("no subject alert found");
    }

    RemoveResult result = removeSubjectFromAlert(alert, subjectName);

    if (result == RemoveResult.NOT_FOUND) {
      throw new RuntimeException("matching subject alert not found");
    }

    String alertId = String.valueOf(alert.getAsJsonPrimitive("id").getAsLong());
    if (result == RemoveResult.EMPTY_AFTER_REMOVE) {
      userApi.deleteObject(String.format("individuals/%s/alerts/%s", nedId, alertId));
    } else {
      userApi.putObject(String.format("individuals/%s/alerts/%s", nedId, alertId), alert);
    }
  }

  /**
   * Check if the logged in user is subscribed to the subject alert or not.
   *
   * @param authId The authentication ID of the logged in user, different from Ned ID
   * @param journalKey The journal key to check for alert in.
   * @param subjectName The subject for which to check for alert.
   * @return whether the user is subscribed to the subject alert in this journal.
   * @throws IOException
   */
  public boolean isUserSubscribed(String authId, String journalKey, String subjectName) throws IOException {

    String nedId = userApi.getUserIdFromAuthId(authId);
    JsonArray alerts = userApi.requestObject(String.format("individuals/%s/alerts", nedId), JsonArray.class);
    JsonObject existing = findMatchingAlert(alerts, journalKey);

    if (existing == null) {
      return false;
    }
    else if (Strings.isNullOrEmpty(subjectName)) {
      // empty subject list means alert for all (or any) subjects.
      return existing != null;
    } else {
      JsonObject query = getAlertQuery(existing);
      JsonArray filterSubjects = query.getAsJsonArray("filterSubjectsDisjunction");
      for (JsonElement subject : filterSubjects) {
        if (subjectName.equalsIgnoreCase(subject.getAsJsonPrimitive().getAsString())) {
          return true;
        }
      }
      return false;
    }
  }


  /**
   * For the list of alerts, return the first alert object that matches the journal, or null.
   * It matches only if there is only one item in "filterJournals" matching the specified journal
   * key.
   *
   * Besides matching for the journal key in "filterJournals" attribute of the alert,
   * it also checks if the "source" is "Ambra", "frequency" is "weekly" and "name" is PLoSONE".
   *
   * @param alerts JSON list of alert objects.
   * @param journalKey The journal key string.
   * @return  First matching alert object for journalKey or null if no match.
   */
  private JsonObject findMatchingAlert(JsonArray alerts, String journalKey) {
    for (JsonElement it : alerts) {
      JsonObject alert = it.getAsJsonObject();
      String source = alert.getAsJsonPrimitive("source").getAsString();
      String frequency = alert.getAsJsonPrimitive("frequency").getAsString();
      String name = alert.getAsJsonPrimitive("name").getAsString();

      if (ALERT_SOURCE.equalsIgnoreCase(source)
          && ALERT_FREQUENCY.equalsIgnoreCase(frequency)
          && ALERT_NAME.equalsIgnoreCase(name)) {
        JsonObject query = getAlertQuery(alert);
        if (query.has("filterJournals") && query.has("filterSubjectsDisjunction")) {
          JsonArray filterJournals = query.getAsJsonArray("filterJournals");
          if (filterJournals.size() == 1 && journalKey.equalsIgnoreCase(filterJournals.get(0).getAsString())) {
            return alert;
          }
        }
      }
    }
    return null;
  }

  /**
   * For the alert object, return the "query" attribute string parsed into a JSON object.
   * Return null if no "query" exist.
   *
   * @param alert The JSON alert object.
   * @return Parsed JSON object for the "query" attribute value, or null if not found.
   */
  private JsonObject getAlertQuery(JsonObject alert) {
    String queryString = alert.getAsJsonPrimitive("query").getAsString();
    if (queryString != null) {
      return (JsonObject) (new Gson()).fromJson(queryString, JsonObject.class);
    }
    return null;
  }

  /**
   * Create a new JSON alert object for the subject in that journal.
   *
   * @param journalKey The journal key.
   * @param subjectName The subject to create alert for.
   * @return JSON alert object.
   */
  private JsonObject createAlertForSubject(String journalKey, String subjectName) {
    String queryString = "{\"startPage\":0,\"filterSubjects\":[],\"unformattedQuery\":\"\",\"query\":\"*:*\","
        + "\"eLocationId\":\"\",\"pageSize\":0,\"resultView\":\"\",\"volume\":\"\",\"sortValue\":\"\","
        + "\"sortKey\":\"\",\"filterKeyword\":\"\",\"filterArticleTypes\":[],"
        + "\"filterSubjectsDisjunction\":[],\"filterAuthors\":[],\"filterJournals\":[],\"id\":\"\"}";
    JsonObject query = (new Gson()).fromJson(queryString, JsonObject.class);

    JsonArray filterJournals = query.getAsJsonArray("filterJournals");
    filterJournals.add(new JsonPrimitive(journalKey));
    JsonArray filterSubjects = query.getAsJsonArray("filterSubjectsDisjunction");
    filterSubjects.add(new JsonPrimitive(subjectName));
    query.remove("filterJournals");
    query.remove("filterSubjects");
    query.add("filterJournals", filterJournals);
    query.add("filterSubjects", filterSubjects);

    JsonObject alert = new JsonObject();
    alert.addProperty("source", ALERT_SOURCE);
    alert.addProperty("frequency", ALERT_FREQUENCY);
    alert.addProperty("name", ALERT_NAME);
    alert.addProperty("query", (new Gson()).toJson(query));

    return alert;
  }

  /**
   * Add the subject in the "filterSubjectsDisjuction" attribute
   * of "query" attribute of the alert JSON object.
   *
   * @param alert
   * @param subjectName
   */
  private void addSubjectToAlert(JsonObject alert, String subjectName) {
    JsonObject query = getAlertQuery(alert);

    JsonArray filterSubjects = query.getAsJsonArray("filterSubjectsDisjunction");
    for (JsonElement filterSubject : filterSubjects) {
      if (subjectName.equalsIgnoreCase(filterSubject.getAsString())) {
        throw new RuntimeException("already exists");
      }
    }

    filterSubjects.add(new JsonPrimitive(subjectName));
    alert.remove("query");
    alert.add("query", new JsonPrimitive((new Gson()).toJson(query)));
  }

  /**
   * Remove a subject from the alert object.
   *
   * @param alert The JSON alert object.
   * @param subjectName
   * @return Indicates whether the subject was not found, or found and removed, and
   * if yes, then whether the "filterSubjects" is empty after remove or not.
   */
  private RemoveResult removeSubjectFromAlert(JsonObject alert, String subjectName) {
    JsonObject query = getAlertQuery(alert);
    JsonArray filterSubjects = query.getAsJsonArray("filterSubjectsDisjunction");

    boolean found = false;
    for (Iterator<JsonElement> it = filterSubjects.iterator(); it.hasNext(); ) {
      JsonElement filterSubject = it.next();
      if (subjectName.equalsIgnoreCase(filterSubject.getAsString())) {
        it.remove();
        found = true;
      }
    }
    if (!found) {
      return RemoveResult.NOT_FOUND;
    }

    query.remove("filterSubjectsDisjunction");
    query.add("filterSubjectsDisjunction", filterSubjects);
    alert.remove("query");
    alert.add("query", new JsonPrimitive((new Gson()).toJson(query)));

    return (filterSubjects.size() == 0 ? RemoveResult.EMPTY_AFTER_REMOVE : RemoveResult.NOT_EMPTY_AFTER_REMOVE);
  }
}
