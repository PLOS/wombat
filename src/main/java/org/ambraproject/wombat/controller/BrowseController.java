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
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    model.addAttribute("articleGroups", buildArticleGroups(site, issueMetadata));

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

  public static class TypedArticleGroup {
    private final ArticleType type;
    private final ImmutableList<Map<String, ?>> articles;

    private TypedArticleGroup(ArticleType type, List<Map<String, ?>> articles) {
      this.type = Objects.requireNonNull(type);
      this.articles = ImmutableList.copyOf(articles);
    }

    public ArticleType getType() {
      return type;
    }

    public ImmutableList<Map<String, ?>> getArticles() {
      return articles;
    }
  }

  private List<TypedArticleGroup> buildArticleGroups(Site site, Map<String, Object> issueMetadata) throws IOException {
    // Ordered list of all articles in the issue.
    List<Map<String, ?>> articles = (List<Map<String, ?>>) issueMetadata.get("articles");

    // Articles grouped by their type. Order within the value lists is significant.
    ListMultimap<ArticleType, Map<String, ?>> groupedArticles = LinkedListMultimap.create();
    for (Map<String, ?> article : articles) {
      if (!article.containsKey("revisionNumber")) continue; // Omit unpublished articles

      ArticleType articleType = ArticleType.get(site.getTheme(), (String) article.get("articleType"));
      groupedArticles.put(articleType, article);
    }

    // The article types supported by this site, in the order in which they are supposed to appear.
    ImmutableList<ArticleType> articleTypes = ArticleType.read(site.getTheme());

    // Produce a correctly ordered list of TypedArticleGroup, populated with the article groups.
    List<TypedArticleGroup> articleGroups = new ArrayList<>(articleTypes.size());
    for (ArticleType articleType : articleTypes) {
      List<Map<String, ?>> articlesOfType = groupedArticles.removeAll(articleType);
      if (!articlesOfType.isEmpty()) {
        articleGroups.add(new TypedArticleGroup(articleType, articlesOfType));
      }
    }

    // If any article groups were not matched, append them to the end.
    for (Map.Entry<ArticleType, List<Map<String, ?>>> entry : Multimaps.asMap(groupedArticles).entrySet()) {
      ArticleType type = entry.getKey();
      TypedArticleGroup group = new TypedArticleGroup(type, entry.getValue());
      articleGroups.add(group);

      log.warn(String.format("Issue %s has articles of type \"%s\", which is not configured for %s: %s",
          issueMetadata.get("doi"), type.getName(), site.getKey(),
          Lists.transform(group.articles, article -> article.get("doi"))));
    }

    return articleGroups;
  }
}
