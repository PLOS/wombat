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
import org.ambraproject.wombat.model.ScholarlyWorkId;
import org.ambraproject.wombat.service.ApiAddress;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.RenderContext;
import org.ambraproject.wombat.service.XmlService;
import org.ambraproject.wombat.service.remote.ArticleApi;
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
import java.util.stream.Collectors;

/**
 * Controller for the browse page.
 */
@Controller
public class BrowseController extends WombatController {

  private static final Logger log = LoggerFactory.getLogger(BrowseController.class);

  @Autowired
  private ArticleApi articleApi;

  @Autowired
  private ArticleService articleService;

  @Autowired
  private ArticleTransformService articleTransformService;

  @Autowired
  private XmlService xmlService;


  @RequestMapping(name = "browseVolumes", value = "/volume")
  public String browseVolume(Model model, @SiteParam Site site) throws IOException {
    Map<String, Map<String, Object>> journalMetadata = articleApi.requestObject(
        ApiAddress.builder("journals").addToken(site.getJournalKey()).build(),
        Map.class);
    String issueDesc = (String) journalMetadata.getOrDefault("currentIssue",
        Collections.emptyMap()).getOrDefault("description", "");
    model.addAttribute("currentIssueDescription",
        articleTransformService.transformImageDescription(new RenderContext(site), issueDesc));
    model.addAttribute("journal", journalMetadata);
    return site.getKey() + "/ftl/browse/volumes";
  }

  private static final ApiAddress ARTICLE_TYPES_ADDRESS = ApiAddress.builder("articleTypes").build();

  @RequestMapping(name = "browseIssues", value = "/issue")
  public String browseIssue(Model model, @SiteParam Site site,
                            @RequestParam(value = "id", required = false) String issueId) throws IOException {

    Map<String, Object> journalMetadata = articleApi.requestObject(
        ApiAddress.builder("journals").addToken(site.getJournalKey()).build(),
        Map.class);
    model.addAttribute("journal", journalMetadata);

    ApiAddress issueMetaUrl = (issueId == null)
        ? ApiAddress.builder("journals").addToken(site.getJournalKey()).addParameter("currentIssue").build()
        : ApiAddress.builder("issues").addToken(issueId).build();
    Map<String, Object> issueMeta;
    try {
      issueMeta = articleApi.requestObject(issueMetaUrl, Map.class);
    } catch (EntityNotFoundException e) {
      throw new NotFoundException(e);
    }
    model.addAttribute("issue", issueMeta);

    String issueDesc = (String) issueMeta.getOrDefault("description", "");
    model.addAttribute("issueTitle", articleTransformService.transformImageDescription(new RenderContext(site),
        xmlService.extractElementFromFragment(issueDesc, "title")));
    model.addAttribute("issueDescription", articleTransformService.transformImageDescription(new RenderContext(site),
        xmlService.removeElementFromFragment(issueDesc, "title")));

    List<Map<String, Object>> articleGroups = articleApi.requestObject(ARTICLE_TYPES_ADDRESS, List.class);

    articleGroups.stream().forEach(ag -> ag.put("articles", new ArrayList<Map<?, ?>>()));

    for (String articleDoi : (List<String>) issueMeta.get("articleOrder")) {
      ScholarlyWorkId articleId = ScholarlyWorkId.of(articleDoi);
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
      Map<String, String> currentArticleType = (Map<String, String>) articleMetadata.get("articleType");
      if (currentArticleType == null || currentArticleType.get("heading") == null) {
        log.warn("No article type found for {}", articleId);
        continue;
      }
      articleGroups.stream()
          .filter(ag -> ag.get("heading").equals(currentArticleType.get("heading")))
          .forEach(ag -> ((ArrayList<Map<?, ?>>) ag.get("articles")).add(articleMetadata));
    }

    articleGroups = articleGroups.stream()
        .filter(ag -> !((ArrayList<Map<?, ?>>) ag.get("articles")).isEmpty())
        .collect(Collectors.toList());

    model.addAttribute("articleGroups", articleGroups);

    return site.getKey() + "/ftl/browse/issues";
  }

}
