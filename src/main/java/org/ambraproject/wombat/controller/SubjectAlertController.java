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

import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.service.remote.SubjectAlertService;
import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller class for subject alert add/delete.
 * This is invoked from Ajax and cannot have DELETE method, so
 * an "action" param is used to indicate "add" or "remove".
 */
@Controller
public class SubjectAlertController extends WombatController {
  private static final Logger log = LoggerFactory.getLogger(SubjectAlertController.class);

  private static final String ACTION_ADD = "add";

  private static final String ACTION_REMOVE = "remove";

  private static final String RESULT_SUCCESS = "success";

  private static final String RESULT_FAILED = "failed";

  @Autowired
  private SubjectAlertService subjectAlertService;

  /**
   * Add/remove subject alert for the logged in user.
   *
   * @param request
   * @param model
   * @param site
   * @param params "action" is either "add" or "remove" and "subject" is string for the alert's subject.
   * @return Map converted to JSON with "result" of "success" or "failed" and optional "error" attributes.
   * @throws IOException
   */

  @RequestMapping(name = "addSubjectAlert", value = "/subjectalert", params = {"action", "subject"})
  @ResponseBody
  public Map<String, Object> changeSubjectAlert(HttpServletRequest request, Model model, @SiteParam Site site,
                                @RequestParam Map<String, String> params)
      throws IOException {

    Map<String, Object> response = new HashMap<String, Object>();
    try {
      String action = params.get("action");
      String subject = params.get("subject");
      subject = subject.replace("_", " ");
      String subjectName = WordUtils.capitalize(subject);

      String journalKey = site.getJournalKey();

      String authId = request.getRemoteUser();
      if (authId == null) {
        throw new RuntimeException("not logged in");
      }

      if (ACTION_ADD.equals(action)) {
        subjectAlertService.addAlert(authId, journalKey, subjectName);
      } else if (ACTION_REMOVE.equals(action)) {
        subjectAlertService.removeAlert(authId, journalKey, subjectName);
      }

      response.put("result", RESULT_SUCCESS);
    } catch (RuntimeException e) {
      e.printStackTrace();
      response.put("result", RESULT_FAILED);
      response.put("error", e.getMessage());
    }
    return response;
  }
}
