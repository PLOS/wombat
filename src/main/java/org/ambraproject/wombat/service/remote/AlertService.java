package org.ambraproject.wombat.service.remote;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.plos.ned_client.model.Alert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Add and remove alert for user using individuals/{userId}/alerts
 * resource.
 */
public class AlertService {

  /**
   * Identifies this component as the source of the alert.
   */
  private static final String ALERT_SOURCE = "Ambra";

  /**
   * The only supported frequency value for this type of alert.
   */
  private static final String ALERT_FREQUENCY = "weekly";

  /**
   * Due to legacy reasons, this is a magic string that uniquely identifies alerts of this type (i.e., for subject
   * areas). Despite how it looks, it is not a journal key.
   */
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
  public static class AlertException extends RuntimeException {
    private AlertException(String message) {
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
  public void addSubjectAlert(String authId, String journalKey, String subjectName) throws IOException {
    String userId = userApi.getUserIdFromAuthId(authId);

    List<Alert> alerts = fetchAlerts(userId);
    Alert alert = findMatchingAlert(alerts, journalKey)
        .map(a -> addSubjectToAlert(a, subjectName))
        .orElseGet(() -> createAlertForSubject(journalKey, subjectName));

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
  public void removeSubjectAlert(String authId, String journalKey, String subjectName) throws IOException {
    String userId = userApi.getUserIdFromAuthId(authId);

    List<Alert> alerts = fetchAlerts(userId);
    Alert alert = findMatchingAlert(alerts, journalKey)
        .orElseThrow(() -> new AlertException("no subject alert found"));

    RemoveResult result = removeSubjectFromAlert(alert, subjectName);

    if (result == RemoveResult.NOT_FOUND) {
      throw new AlertException("matching subject alert not found");
    }

    String alertId = alert.getId().toString();
    if (result == RemoveResult.EMPTY_AFTER_REMOVE) {
      userApi.deleteObject(String.format("individuals/%s/alerts/%s", userId, alertId));
    } else {
      userApi.putObject(String.format("individuals/%s/alerts/%s", userId, alertId), alert);
    }
  }

  public void addSearchAlert(String authId, String name, String query,
                             String frequency) throws IOException {
    String userId = userApi.getUserIdFromAuthId(authId);
    Alert alert = createAlertForSearch(name, query, frequency);
    userApi.postObject(String.format("individuals/%s/alerts", userId), alert);
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
    Optional<Alert> existing = findMatchingAlert(alerts, journalKey);

    if (!existing.isPresent()) {
      return false;
    } else {
      AlertQuery query = getAlertQuery(existing.get());
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
   * For the list of alerts, return the first alert object that matches the journal, or empty.
   * It matches only if there is only one item in "filterJournals" matching the specified journal
   * key.
   *
   * Besides matching for the journal key in "filterJournals" attribute of the alert,
   * it also checks if the "source" is "Ambra", "frequency" is "weekly" and "name" is PLoSONE".
   *
   * @param alerts JSON list of alert objects.
   * @param journalKey The journal key string.
   * @return  First matching alert object for journalKey or empty if no match.
   */
  private Optional<Alert> findMatchingAlert(List<Alert> alerts, String journalKey) {
    for (Alert alert : alerts) {
      String source = alert.getSource();
      String frequency = alert.getFrequency();
      String name = alert.getName();

      if (ALERT_SOURCE.equalsIgnoreCase(source)
          && ALERT_FREQUENCY.equalsIgnoreCase(frequency)
          && ALERT_NAME.equalsIgnoreCase(name)) {
        AlertQuery query = getAlertQuery(alert);
        List<String> filterJournals = query.getFilterJournals();
        if (filterJournals.size() == 1 && journalKey.equals(filterJournals.get(0))) {
          return Optional.of(alert);
        }
      }
    }
    return Optional.empty();
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
    query.setFilterSubjectsDisjunction(ImmutableList.of(subjectName));

    Alert alert = new Alert();
    alert.setSource(ALERT_SOURCE);
    alert.setFrequency(ALERT_FREQUENCY);
    alert.setName(ALERT_NAME);
    alert.setQuery(gson.toJson(query));

    return alert;
  }

  private Alert createAlertForSearch(String name, String query, String frequency) {
    Alert alert = new Alert();
    alert.setSource(ALERT_SOURCE);
    alert.setFrequency(frequency);
    alert.setName(name);
    alert.setQuery(query);

    return alert;
  }

  /**
   * Add the subject in the "filterSubjectsDisjuction" attribute
   * of "query" attribute of the alert JSON object.
   *
   * @param alert
   * @param subjectName
   */
  private Alert addSubjectToAlert(Alert alert, String subjectName) {
    AlertQuery query = getAlertQuery(alert);

    List<String> filterSubjects = query.getFilterSubjectsDisjunction();
    for (String filterSubject : filterSubjects) {
      if (subjectName.equalsIgnoreCase(filterSubject)) {
        throw new AlertException("already exists");
      }
    }

    query.setFilterSubjectsDisjunction(ImmutableList.<String>builder()
        .addAll(filterSubjects).add(subjectName).build());
    alert.setQuery(gson.toJson(query));

    return alert;
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

  // convert params to JSON model attribute to be used as alert query if needed.
  public String convertParamsToJSON(MultiValueMap<String, String> params) {
    Map<String, Object> map = new HashMap<String, Object>();
    for (Map.Entry<String, List<String> > entry: params.entrySet()) {
      String key = entry.getKey();
      if (key == "q") {
        key = "query";
      }
      if (key.equals("filterSubjects") || key.equals("filterArticleTypes") || key.equals("filterSections")
          || key.equals("filterSubjectsDisjunction") || key.equals("filterAuthors") || key.equals("filterJournals")) {
        map.put(key, entry.getValue());
      } else {
        List<String> value = entry.getValue();
        map.put(key, value.isEmpty() ? "" : value.get(0));
      }
    }
    // need to create single line JSON, so cannot use the gson bean.
    String result = (new Gson()).toJson(map);
    return result;
  }
}
