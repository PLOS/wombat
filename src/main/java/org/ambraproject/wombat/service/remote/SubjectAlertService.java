package org.ambraproject.wombat.service.remote;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.plos.ned_client.model.Alert;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Add and remove subject alert for user in journal using individuals/{userId}/alerts
 * resource.
 */
public class SubjectAlertService {

  private static final String ALERT_SOURCE = "Ambra";

  private static final String ALERT_FREQUENCY = "weekly";

  private static final String ALERT_NAME = "PLoSONE";

  @Autowired
  private UserApi userApi;
  @Autowired
  private Gson gson;

  private static enum RemoveResult {
    NOT_FOUND, EMPTY_AFTER_REMOVE, NOT_EMPTY_AFTER_REMOVE
  }

  /**
   * Indicates that a subject alert operation could not be completed due to invalid input or state.
   */
  public static class SubjectAlertException extends RuntimeException {
    private SubjectAlertException(String message) {
      super(message);
    }
  }


  private static final Type LIST_OF_ALERTS = new TypeToken<List<Alert>>() {
  }.getType();

  private List<Alert> fetchAlerts(String userId) throws IOException {
    return userApi.requestObject(String.format("individuals/%s/alerts", Objects.requireNonNull(userId)), LIST_OF_ALERTS);
  }

  /**
   * Add the subject alert for the loggedin user for the journal.
   * If the alert object exists for the journal it uses PUT individuals/{userId}/alerts/{alertId}
   * to modify that alert object, otherwise it uses POST individuals/{userId}/alerts
   * to create a new alert object.
   *
   * @param authId The authentication ID of the logged in user.
   * @param journalKey The journal key.
   * @param subjectName The subject to add alert for.
   * @throws IOException
   */
  public void addAlert(String authId, String journalKey, String subjectName) throws IOException {
    String userId = userApi.getUserIdFromAuthId(authId);
    if (userId == null) {
      throw new SubjectAlertException("failed to get NED ID");
    }

    List<Alert> alerts = fetchAlerts(userId);
    Alert alert = findMatchingAlert(alerts, journalKey);

    if (alert != null) {
      addSubjectToAlert(alert, subjectName);
    } else {
      alert = createAlertForSubject(journalKey, subjectName);
    }

    if (alert.getId() != null) {
      String alertId = alert.getId().toString();
      userApi.putObject(String.format("individuals/%s/alerts/%s", userId, alertId), alert);
    } else {
      userApi.postObject(String.format("individuals/%s/alerts", userId), alert);
    }
  }

  /**
   * Remove the subject alert for the loggedin user for the journal.
   * After removing the subject, if other subjects exist in the alert object for the journal,
   * then it uses PUT individuals/{userId}/alerts/{alertId} to modify the alert object,
   * otherwise it uses DELETE individuals/{userId}/alerts/{alertId} to remove the
   * alert object.
   *
   * @param authId The authentication ID of the logged in user.
   * @param journalKey The journal key.
   * @param subjectName The subject to remove alert for.
   * @throws IOException
   */
  public void removeAlert(String authId, String journalKey, String subjectName) throws IOException {
    String userId = userApi.getUserIdFromAuthId(authId);
    if (userId == null) {
      throw new SubjectAlertException("failed to get NED ID");
    }

    List<Alert> alerts = fetchAlerts(userId);
    Alert alert = findMatchingAlert(alerts, journalKey);

    if (alert == null) {
      throw new SubjectAlertException("no subject alert found");
    }

    RemoveResult result = removeSubjectFromAlert(alert, subjectName);

    if (result == RemoveResult.NOT_FOUND) {
      throw new SubjectAlertException("matching subject alert not found");
    }

    String alertId = alert.getId().toString();
    if (result == RemoveResult.EMPTY_AFTER_REMOVE) {
      userApi.deleteObject(String.format("individuals/%s/alerts/%s", userId, alertId));
    } else {
      userApi.putObject(String.format("individuals/%s/alerts/%s", userId, alertId), alert);
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

    String userId = userApi.getUserIdFromAuthId(authId);
    List<Alert> alerts = fetchAlerts(userId);
    Alert existing = findMatchingAlert(alerts, journalKey);

    if (existing == null) {
      return false;
    }
    else if (Strings.isNullOrEmpty(subjectName)) {
      // empty subject list means alert for all (or any) subjects.
      return existing != null;
    } else {
      AlertQuery query = getAlertQuery(existing);
      List<String> filterSubjects = query.getFilterSubjectsDisjunction();
      for (String subject : filterSubjects) {
        if (subjectName.equalsIgnoreCase(subject)) {
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
  private Alert findMatchingAlert(List<Alert> alerts, String journalKey) {
    for (Alert alert : alerts) {
      String source = alert.getSource();
      String frequency = alert.getFrequency();
      String name = alert.getName();

      if (ALERT_SOURCE.equalsIgnoreCase(source)
          && ALERT_FREQUENCY.equalsIgnoreCase(frequency)
          && ALERT_NAME.equalsIgnoreCase(name)) {
        AlertQuery query = getAlertQuery(alert);
        List<String> filterJournals = query.getFilterJournals();
        if (filterJournals.size() == 1 && journalKey.equalsIgnoreCase(filterJournals.get(0))) {
          return alert;
        }
      }
    }
    return null;
  }

  /**
   * For the alert object, return the "query" attribute string parsed into a JSON object.
   *
   * @param alert The JSON alert object.
   * @return Parsed JSON object for the "query" attribute value
   */
  private AlertQuery getAlertQuery(Alert alert) {
    String queryString = Objects.requireNonNull(alert.getQuery());
    return gson.fromJson(queryString, AlertQuery.class);
  }

  /**
   * Create a new JSON alert object for the subject in that journal.
   *
   * @param journalKey The journal key.
   * @param subjectName The subject to create alert for.
   * @return JSON alert object.
   */
  private Alert createAlertForSubject(String journalKey, String subjectName) {
    AlertQuery query = new AlertQuery();

    query.setFilterJournals(ImmutableList.of(journalKey));

    List<String> subjectList = ImmutableList.of(subjectName);
    query.setFilterSubjects(subjectList);
    query.setFilterSubjectsDisjunction(subjectList);

    Alert alert = new Alert();
    alert.setSource(ALERT_SOURCE);
    alert.setFrequency(ALERT_FREQUENCY);
    alert.setName(ALERT_NAME);
    alert.setQuery(gson.toJson(query));

    return alert;
  }

  /**
   * Add the subject in the "filterSubjectsDisjuction" attribute
   * of "query" attribute of the alert JSON object.
   *
   * @param alert
   * @param subjectName
   */
  private void addSubjectToAlert(Alert alert, String subjectName) {
    AlertQuery query = getAlertQuery(alert);

    List<String> filterSubjects = query.getFilterSubjectsDisjunction();
    for (String filterSubject : filterSubjects) {
      if (subjectName.equalsIgnoreCase(filterSubject)) {
        throw new SubjectAlertException("already exists");
      }
    }

    query.setFilterSubjectsDisjunction(ImmutableList.<String>builder()
        .addAll(filterSubjects).add(subjectName).build());
    alert.setQuery(gson.toJson(query));
  }

  /**
   * Remove a subject from the alert object.
   *
   * @param alert The JSON alert object.
   * @param subjectName
   * @return Indicates whether the subject was not found, or found and removed, and
   * if yes, then whether the "filterSubjects" is empty after remove or not.
   */
  private RemoveResult removeSubjectFromAlert(Alert alert, String subjectName) {
    AlertQuery query = getAlertQuery(alert);
    List<String> filterSubjects = new LinkedList<>(query.getFilterSubjectsDisjunction());

    boolean found = false;
    for (Iterator<String> it = filterSubjects.iterator(); it.hasNext(); ) {
      String filterSubject = it.next();
      if (subjectName.equalsIgnoreCase(filterSubject)) {
        it.remove();
        found = true;
      }
    }
    if (!found) {
      return RemoveResult.NOT_FOUND;
    }

    query.setFilterSubjectsDisjunction(filterSubjects);
    alert.setQuery(gson.toJson(query));

    return (filterSubjects.size() == 0 ? RemoveResult.EMPTY_AFTER_REMOVE : RemoveResult.NOT_EMPTY_AFTER_REMOVE);
  }
}
