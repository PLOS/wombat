/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
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
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.RenderContext;
import org.ambraproject.wombat.service.remote.SoaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Controller for the browse page.
 */
@Controller
public class BrowseController extends WombatController {

  private static final Logger log = LoggerFactory.getLogger(BrowseController.class);

  @Autowired
  private SoaService soaService;

  @Autowired
  private ArticleService articleService;

  @Autowired
  private ArticleTransformService articleTransformService;


  @RequestMapping(name = "browseVolumes", value = "/volume")
  public String browseVolume(Model model, @SiteParam Site site) throws IOException {
    String journalMetaUrl = "journals/" + site.getJournalKey();
    Map<String, Map<String, Object>> journalMetadata = soaService.requestObject(journalMetaUrl, Map.class);
    String issueDesc = (String) journalMetadata.getOrDefault("currentIssue",
        Collections.emptyMap()).getOrDefault("description", "");
    model.addAttribute("currentIssueDescription",
        articleTransformService.transformImageDescription(new RenderContext(site), issueDesc));
    model.addAttribute("journal", journalMetadata);
    return site.getKey() + "/ftl/browse/volumes";
  }

  @RequestMapping(name = "browseIssues", value = "/issue")
  public String browseIssue(Model model, @SiteParam Site site,
                            @RequestParam(value = "id", required = false) String issueId) throws IOException {

    String journalMetaUrl = "journals/" + site.getJournalKey();
    Map<String, Object> journalMetadata = soaService.requestObject(journalMetaUrl, Map.class);
    model.addAttribute("journal", journalMetadata);

    String issueMetaUrl = issueId == null ? "journals/" + site.getJournalKey() + "?currentIssue" : "issues/" + issueId;
    Map<String, Object> issueMeta;
    try {
      issueMeta = soaService.requestObject(issueMetaUrl, Map.class);
    } catch (EntityNotFoundException e) {
      throw new NotFoundException(e);
    }
    model.addAttribute("issue", issueMeta);

    String[] parsedIssueInfo = extractInfoFromIssueDesc((String)issueMeta.get("description"));
    model.addAttribute("issueTitle", parsedIssueInfo[0]);
    model.addAttribute("issueImageCredit", parsedIssueInfo[1]);
    model.addAttribute("issueDescription", parsedIssueInfo[2]);

    List<Map<String, Object>> articleGroups = soaService.requestObject("articleTypes", List.class);

    articleGroups.stream().forEach(ag -> ag.put("articles", new ArrayList<Map<?, ?>>()));

    for (String articleId : (List<String>)issueMeta.get("articleOrder")) {
      Map<?, ?> articleMetadata;
      try {
        articleMetadata = articleService.requestArticleMetadata(articleId, true);
      } catch (EntityNotFoundException e) {
        throw new ArticleNotFoundException(articleId);
      }
      try {
        validateArticleVisibility(site, articleMetadata);
      } catch (NotVisibleException e) {
        continue; //skip any articles that should be hidden from view
      }
      Map<String, String> currentArticleType = (Map<String, String>)articleMetadata.get("articleType");
      if (currentArticleType == null || currentArticleType.get("heading") == null) {
        log.warn("No article type found for {}", articleId);
        continue;
      }
      articleGroups.stream()
          .filter(ag -> ag.get("heading").equals(currentArticleType.get("heading")))
          .forEach(ag -> ((ArrayList<Map<?, ?>>)ag.get("articles")).add(articleMetadata));
    }

    articleGroups = articleGroups.stream()
        .filter(ag -> !((ArrayList<Map<?, ?>>)ag.get("articles")).isEmpty())
        .collect(Collectors.toList());

    model.addAttribute("articleGroups", articleGroups);

    return site.getKey() + "/ftl/browse/issues";
  }

  // TODO: get rid of this bit of ugliness from old Ambra if possible, or at least move regex into themes
  /**
   * Extract issue title, issue description, issue image credit from the full issue description
   * @param desc full issue description
   * @return issue title, issue image credit, issue description
   */
  private String[] extractInfoFromIssueDesc(String desc) {
    String results[] = {"", "", ""};
    int start = 0, end = 0;

    // get the title of the issue
    Pattern p1 = Pattern.compile("<title>(.*?)</title>");
    Matcher m1 = p1.matcher(desc);
    if (m1.find()) {
      // there should be one title
      results[0] = m1.group(1);
      // title seems to be surround by <bold> element
      results[0] = results[0].replaceAll("<.*?>", "");

      start = m1.start();
      end = m1.end();

      // remove the title from the total description
      String descBefore = desc.substring(0, start);
      String descAfter = desc.substring(end);
      desc = descBefore + descAfter;
    }

    // get the image credit
    Pattern p2 = Pattern.compile("<italic>Image Credit: (.*?)</italic>");
    Matcher m2 = p2.matcher(desc);
    if (m2.find()) {
      // there should be one image credit
      results[1] = m2.group(1);

      start = m2.start();
      end = m2.end();

      // remove the image credit from the total description
      String descBefore = desc.substring(0, start);
      String descAfter = desc.substring(end);
      desc = descBefore + descAfter;
    }

    // once title and image credit have been removed, the rest of the content is the issue description
    results[2] = desc;

    return results;
  }
}
