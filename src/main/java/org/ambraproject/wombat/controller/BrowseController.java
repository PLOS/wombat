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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.model.ArticleType;
import org.ambraproject.wombat.service.ApiAddress;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.XmlUtil;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
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
  private ArticleMetadata.Factory articleMetadataFactory;

  @RequestMapping(name = "browseVolumes", value = "/volume")
  public String browseVolume(Model model, @SiteParam Site site) throws IOException {
    Map<String, Object> journalMetadata = modelJournalMetadata(model, site);
    String imageArticleDoi = (String) ((Map) ((Map) journalMetadata.get("currentIssue")).get("imageArticle")).get("doi");
    transformIssueImageMetadata(model, site, imageArticleDoi);

    return site.getKey() + "/ftl/browse/volumes";
  }

  @RequestMapping(name = "browseIssues", value = "/issue")
  public String browseIssue(Model model, @SiteParam Site site,
                            @RequestParam(value = "id", required = false) String issueId) throws IOException {

    modelJournalMetadata(model, site);

    ApiAddress readIssueUrl = (issueId == null)
        ? ApiAddress.builder("journals").addToken(site.getJournalKey()).addParameter("currentIssue").build()
        : ApiAddress.builder("issues").embedDoi(issueId).build();
    Map<String, Object> issueMetadata = getIssueMetadata(readIssueUrl);
    model.addAttribute("issue", issueMetadata);

    String imageArticleDoi = (String) ((Map) issueMetadata.get("imageArticle")).get("doi");
    transformIssueImageMetadata(model, site, imageArticleDoi);

    modelArticleGroups(model, site, issueMetadata);

    return site.getKey() + "/ftl/browse/issues";
  }

  private Map<String, Object> modelJournalMetadata(Model model, @SiteParam Site site) throws IOException {
    Map<String, Object> journalMetadata = articleApi.requestObject(
        ApiAddress.builder("journals").addToken(site.getJournalKey()).build(),
        Map.class);
    model.addAttribute("journal", journalMetadata);
    return journalMetadata;
  }

  private Map<String, Object> getIssueMetadata(ApiAddress issueMetaUrl) throws IOException {
    Map<String, Object> issueMeta;
    try {
      issueMeta = articleApi.requestObject(issueMetaUrl, Map.class);
    } catch (EntityNotFoundException e) {
      throw new NotFoundException(e);
    }
    return issueMeta;
  }

  private void transformIssueImageMetadata(Model model, Site site, String imageArticleDoi) throws IOException {
    RequestedDoiVersion requestedDoiVersion = RequestedDoiVersion.of(imageArticleDoi);
    ArticleMetadata imageArticleMetadata = articleMetadataFactory.get(site, requestedDoiVersion);
    ArticlePointer issueImageArticleId = imageArticleMetadata.getArticlePointer();
    String issueDesc = (String) imageArticleMetadata.getIngestionMetadata().get("description");

    model.addAttribute("issueTitle", articleTransformService.transformImageDescription(site, issueImageArticleId,
        XmlUtil.extractElement(issueDesc, "title")));
    model.addAttribute("issueDescription", articleTransformService.transformImageDescription(site, issueImageArticleId,
        XmlUtil.removeElement(issueDesc, "title")));
  }

  private static class TypedArticleGroup {
    private final ArticleType articleType;
    private final ImmutableList<Map<String, ?>> articles;

    private TypedArticleGroup(ArticleType articleType, List<Map<String, ?>> articles) {
      this.articleType = articleType;
      this.articles = ImmutableList.copyOf(articles);
    }
  }

  private void modelArticleGroups(Model model, Site site, Map<String, Object> issueMetadata) throws IOException {
    List<Map<String, ?>> articles = (List<Map<String, ?>>) issueMetadata.get("articles");
    ListMultimap<String, Map<String, ?>> groupedArticles = Multimaps.index(articles, article -> (String) article.get("articleType"));

    ImmutableList<ArticleType> articleTypes = ArticleType.read(site.getTheme());
    List<TypedArticleGroup> articleGroups = articleTypes.stream()
        .map((ArticleType articleType) -> new TypedArticleGroup(articleType, groupedArticles.get(articleType.getName())))
        .filter((TypedArticleGroup articleGroup) -> !articleGroup.articles.isEmpty())
        .collect(Collectors.toList());

    model.addAttribute("articleGroups", articleGroups);
  }
}
