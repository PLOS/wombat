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

package org.ambraproject.wombat.controller;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
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

  @RequestMapping(name = "addSubjectAlert", value = "/subjectalert/add", method = RequestMethod.POST)
  @ResponseBody
  public Map<String, Object> addSubjectAlert(HttpServletRequest request, @SiteParam Site site,
                                             @RequestParam("subject") String subject)
      throws IOException {
    return changeAlert(request, site, subject, alertService::addSubjectAlert);
  }

  @RequestMapping(name = "removeSubjectAlert", value = "/subjectalert/remove", method = RequestMethod.POST)
  @ResponseBody
  public Map<String, Object> removeSubjectAlert(HttpServletRequest request, @SiteParam Site site,
                                                @RequestParam("subject") String subject)
      throws IOException {
    return changeAlert(request, site, subject, alertService::removeSubjectAlert);
  }

  @RequestMapping(name = "addSearchAlert", value = "/searchalert/add", method = RequestMethod.POST)
  @ResponseBody
  public Map<String, Object> addSearchAlert(HttpServletRequest request, @SiteParam Site site,
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
  private Map<String, Object> changeAlert(HttpServletRequest request, Site site,
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
