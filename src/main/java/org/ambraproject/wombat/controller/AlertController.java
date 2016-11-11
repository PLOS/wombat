/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2016 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.wombat.controller;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.config.site.JournalSite;
import org.ambraproject.wombat.config.site.JournalSpecific;
import org.ambraproject.wombat.service.AlertService;
import org.ambraproject.wombat.service.remote.UserApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controller class for subject alert add/delete. This is invoked from Ajax and cannot have DELETE method, so an
 * "action" param is used to indicate "add" or "remove".
 */
@Controller
public class AlertController extends WombatController {
  private static final Logger log = LoggerFactory.getLogger(AlertController.class);

  @Autowired
  private AlertService alertService;

  @JournalSpecific
  @RequestMapping(name = "addSubjectAlert", value = "/subjectalert/add", method = RequestMethod.POST)
  @ResponseBody
  public Map<String, Object> addSubjectAlert(HttpServletRequest request, JournalSite site,
                                             @RequestParam("subject") String subject)
      throws IOException {
    return changeAlert(request, site, subject, alertService::addSubjectAlert);
  }

  @JournalSpecific
  @RequestMapping(name = "removeSubjectAlert", value = "/subjectalert/remove", method = RequestMethod.POST)
  @ResponseBody
  public Map<String, Object> removeSubjectAlert(HttpServletRequest request, JournalSite site,
                                                @RequestParam("subject") String subject)
      throws IOException {
    return changeAlert(request, site, subject, alertService::removeSubjectAlert);
  }

  @JournalSpecific
  @RequestMapping(name = "addSearchAlert", value = "/searchalert/add", method = RequestMethod.POST)
  @ResponseBody
  public Map<String, Object> addSearchAlert(HttpServletRequest request, JournalSite site,
                                            @RequestParam("name") String name,
                                            @RequestParam("query") String query,
                                            @RequestParam("frequency") List<String> frequency)
      throws IOException {
    if (name.isEmpty()) {
      log.error("Empty name parameter");
      return respondFailure("name required");
    }

    String authId = request.getRemoteUser();
    if (authId == null) {
      log.error("User made request to change search alert without being logged in.");
      return respondFailure("not logged in");
    }

    try {
      for (String freq: frequency) {
        alertService.addSearchAlert(authId, name, query, freq);
      }
    } catch (AlertService.AlertException e) {
      log.error("Error while changing search alert", e);
      return respondFailure(e.getMessage());
    } catch (UserApi.UserApiException e) {
      log.error("Error from UserApi while changing search alert", e);
      return respondFailure("User data is temporarily unavailable");
    }

    return SUCCESS_RESPONSE;
  }

  @FunctionalInterface
  private static interface SubjectAlertAction {
    void execute(String authId, String journalKey, String subjectName) throws IOException;
  }

  private static final ImmutableMap<String, Object> SUCCESS_RESPONSE = ImmutableMap.of("result", "success");

  private static ImmutableMap<String, Object> respondFailure(String message) {
    return ImmutableMap.<String, Object>builder()
        .put("result", "failed")
        .put("error", Strings.nullToEmpty(message))
        .build();
  }

  /**
   * Add/remove subject alert for the logged in user.
   *
   * @return Map converted to JSON with "result" of "success" or "failed" and optional "error" attributes.
   * @throws IOException
   */
  private Map<String, Object> changeAlert(HttpServletRequest request, JournalSite site,
                                                 String subject, SubjectAlertAction action)
      throws IOException {
    if (subject.isEmpty()) {
      log.error("Empty subject parameter");
      return respondFailure("Subject required");
    }

    String authId = request.getRemoteUser();
    if (authId == null) {
      log.error("User made request to change subject alert without being logged in.");
      return respondFailure("not logged in");
    }

    try {
      action.execute(authId, site.getJournalKey(), subject);
    } catch (AlertService.AlertException e) {
      log.error("Error while changing subject alert", e);
      return respondFailure(e.getMessage());
    } catch (UserApi.UserApiException e) {
      log.error("Error from UserApi while changing subject alert", e);
      return respondFailure("User data is temporarily unavailable");
    }

    return SUCCESS_RESPONSE;
  }
}
