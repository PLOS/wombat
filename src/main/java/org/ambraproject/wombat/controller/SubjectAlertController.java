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
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.service.remote.SubjectAlertService;
import org.ambraproject.wombat.service.remote.UserApi;
import org.apache.commons.lang.WordUtils;
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
import java.util.Map;

/**
 * Controller class for subject alert add/delete. This is invoked from Ajax and cannot have DELETE method, so an
 * "action" param is used to indicate "add" or "remove".
 */
@Controller
public class SubjectAlertController extends WombatController {
  private static final Logger log = LoggerFactory.getLogger(SubjectAlertController.class);

  @Autowired
  private SubjectAlertService subjectAlertService;

  @RequestMapping(name = "addSubjectAlert", value = "/subjectalert/add", method = RequestMethod.POST)
  @ResponseBody
  public Map<String, Object> addSubjectAlert(HttpServletRequest request, @SiteParam Site site,
                                             @RequestParam("subject") String subject)
      throws IOException {
    return changeSubjectAlert(request, site, subject, subjectAlertService::addAlert);
  }

  @RequestMapping(name = "removeSubjectAlert", value = "/subjectalert/remove", method = RequestMethod.POST)
  @ResponseBody
  public Map<String, Object> removeSubjectAlert(HttpServletRequest request, @SiteParam Site site,
                                                @RequestParam("subject") String subject)
      throws IOException {
    return changeSubjectAlert(request, site, subject, subjectAlertService::removeAlert);
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
  private Map<String, Object> changeSubjectAlert(HttpServletRequest request, Site site,
                                                 String subject, SubjectAlertAction action)
      throws IOException {
    subject = subject.replace("_", " ");
    String subjectName = WordUtils.capitalize(subject);

    String journalKey = site.getJournalKey();

    String authId = request.getRemoteUser();
    if (authId == null) {
      return respondFailure("not logged in");
    }

    try {
      action.execute(authId, journalKey, subjectName);
    } catch (SubjectAlertService.SubjectAlertException e) {
      log.error("Error while changing subject alert", e);
      return respondFailure(e.getMessage());
    } catch (UserApi.UserApiException e) {
      log.error("Error from UserApi while changing subject alert", e);
      return respondFailure("User data is temporarily unavailable");
    }

    return SUCCESS_RESPONSE;
  }
}
