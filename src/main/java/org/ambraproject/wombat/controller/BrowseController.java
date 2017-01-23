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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.model.ArticleType;
import org.ambraproject.wombat.model.RelatedArticle;
import org.ambraproject.wombat.service.ApiAddress;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.EntityNotFoundException;
import org.ambraproject.wombat.service.SolrArticleAdapter;
import org.ambraproject.wombat.service.XmlUtil;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.SolrSearchApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Controller for the browse page.
 */
@Controller
public class BrowseController extends WombatController {

  private static final Logger log = LoggerFactory.getLogger(BrowseController.class);

  @Autowired
  private ArticleApi articleApi;
  @Autowired
  private ArticleTransformService articleTransformService;
  @Autowired
  private ArticleMetadata.Factory articleMetadataFactory;
  @Autowired
  private SolrSearchApi solrSearchApi;
  @Autowired
  private SiteSet siteSet;
  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;

  /**
   * Validate that an issue belongs to the current site. If not, throw an exception
   * indicating that the user should be redirected to the appropriate site
   */
  private void validateIssueSite(Site site, Map<String, ?> issueMetadata) throws IOException {
    String issueId = (String) issueMetadata.get("doi");
    Map<String, String> parentVolumeMetadata = (Map<String, String>) issueMetadata.get("parentVolume");
    String publishedJournalKey = parentVolumeMetadata.get("journalKey");
    String siteJournalKey = site.getJournalKey();
    if (!publishedJournalKey.equals(siteJournalKey)) {
      Link link = buildCrossSiteRedirectToIssue(publishedJournalKey, site, issueId);
      throw new InternalRedirectException(link);
    }
  }

  private Link buildCrossSiteRedirectToIssue(String targetJournal, Site site, String id) {
    Site targetSite = site.getTheme().resolveForeignJournalKey(siteSet, targetJournal);
    return Link.toForeignSite(site, targetSite)
        .toPattern(requestMappingContextDictionary, "browseIssues")
        .addQueryParameters(ImmutableMap.of("id", id))
        .build();
  }

  @RequestMapping(name = "browseVolumes", value = "/volume")
  public String browseVolume(Model model, @SiteParam Site site) throws IOException {
    Map<String, ?> currentIssue = getCurrentIssue(site).orElse(null);
    if (currentIssue != null) {
      Map<String, ?> imageArticle = (Map<String, ?>) currentIssue.get("imageArticle");
      if (imageArticle != null) {
        String imageArticleDoi = (String) imageArticle.get("doi");
        transformIssueImageMetadata(model, site, imageArticleDoi);
      }
      model.addAttribute("currentIssue", currentIssue);
    }

    String journalKey = site.getJournalKey();
    List<Map<String, ?>> volumes = articleApi.requestObject(
        ApiAddress.builder("journals").addToken(journalKey).addToken("volumes").build(),
        List.class);
    List<Map<String, ?>> populatedVolumes = volumes.parallelStream()
        .map(volume -> populateVolumeWithIssues(journalKey, volume))
        .collect(Collectors.toList());
    model.addAttribute("volumes", populatedVolumes);

    return site.getKey() + "/ftl/browse/volumes";
  }

  private Map<String, ?> populateVolumeWithIssues(String journalKey, Map<String, ?> volume) {
    Map<String, Object> populatedVolume = new HashMap<>(volume);

    List<Map<String, ?>> issues;
    try {
      issues = articleApi.requestObject(
          ApiAddress.builder("journals").addToken(journalKey)
              .addToken("volumes").embedDoi((String) volume.get("doi"))
              .addToken("issues").build(),
          List.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    populatedVolume.put("issues", issues);

    return populatedVolume;
  }

  @RequestMapping(name = "browseIssues", value = "/issue")
  public String browseIssue(Model model, @SiteParam Site site,
                            @RequestParam(value = "id", required = false) String issueId) throws IOException {
    model.addAttribute("journal", fetchJournalMetadata(site));

    Map<String, ?> issueMetadata = (issueId == null)
        ? getCurrentIssue(site).orElseThrow(() -> new RuntimeException("Current issue is not set for " + site.getJournalKey()))
        : getIssue(issueId);
    issueId = (String) issueMetadata.get("doi");

    validateIssueSite(site, issueMetadata);

    model.addAttribute("issue", issueMetadata);

    Map<String, ?> imageArticle = (Map<String, ?>) issueMetadata.get("imageArticle");
    if (imageArticle != null) {
      String imageArticleDoi = (String) (imageArticle).get("doi");
      transformIssueImageMetadata(model, site, imageArticleDoi);
    }

    List<Map<String, ?>> articlesInIssue = articleApi.requestObject(
        ApiAddress.builder("issues").embedDoi(issueId).addToken("contents").build(),
        List.class);
    model.addAttribute("articleGroups", buildArticleGroups(site, issueId, articlesInIssue));

    return site.getKey() + "/ftl/browse/issues";
  }

  private Map<String, Object> fetchJournalMetadata(@SiteParam Site site) throws IOException {
    return articleApi.requestObject(
        ApiAddress.builder("journals").addToken(site.getJournalKey()).build(),
        Map.class);
  }

  private Optional<Map<String, ?>> getCurrentIssue(Site site) throws IOException {
    try {
      Map<String, ?> currentIssue = articleApi.requestObject(ApiAddress.builder("journals")
              .addToken(site.getJournalKey()).addToken("currentIssue").build(),
          Map.class);
      return Optional.of(currentIssue);
    } catch (EntityNotFoundException e) {
      return Optional.empty();
    }
  }

  private Map<String, ?> getIssue(String issueId) throws IOException {
    ApiAddress issueAddress = ApiAddress.builder("issues").embedDoi(issueId).build();
    try {
      return articleApi.requestObject(issueAddress, Map.class);
    } catch (EntityNotFoundException e) {
      throw new NotFoundException(e);
    }
  }

  private void transformIssueImageMetadata(Model model, Site site, String imageArticleDoi) throws IOException {
    RequestedDoiVersion requestedDoiVersion = RequestedDoiVersion.of(imageArticleDoi);
    ArticleMetadata imageArticleMetadata;
    try {
      imageArticleMetadata = articleMetadataFactory.get(site, requestedDoiVersion);
    } catch (NotFoundException e) {
      throw new RuntimeException("Issue's image article not found: " + requestedDoiVersion, e);
    }
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

    private TypedArticleGroup(ArticleType type, List<Map<String, Object>> articles) {
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

  private List<TypedArticleGroup> buildArticleGroups(Site site, String issueId, List<Map<String, ?>> articles)
      throws IOException {
    // Articles grouped by their type. Order within the value lists is significant.
    ArticleType.Dictionary typeDictionary = ArticleType.getDictionary(site.getTheme());
    ListMultimap<ArticleType, Map<String, Object>> groupedArticles = LinkedListMultimap.create();
    for (Map<String, ?> article : articles) {
      if (!article.containsKey("revisionNumber")) continue; // Omit unpublished articles

      Map<String, Object> populatedArticle = new HashMap<>(article);

      Map<String, ?> ingestion = (Map<String, ?>) article.get("ingestion");
      ArticleType articleType = typeDictionary.lookUp((String) ingestion.get("articleType"));

      populateRelatedArticles(populatedArticle);

      populateAuthors(populatedArticle);

      groupedArticles.put(articleType, populatedArticle);
    }

    // The article types supported by this site, in the order in which they are supposed to appear.
    ImmutableList<ArticleType> articleTypes = typeDictionary.getSequence();

    // Produce a correctly ordered list of TypedArticleGroup, populated with the article groups.
    List<TypedArticleGroup> articleGroups = new ArrayList<>(articleTypes.size());
    for (ArticleType articleType : articleTypes) {
      List<Map<String, Object>> articlesOfType = groupedArticles.removeAll(articleType);
      if (!articlesOfType.isEmpty()) {
        articleGroups.add(new TypedArticleGroup(articleType, articlesOfType));
      }
    }

    // If any article groups were not matched, append them to the end.
    for (Map.Entry<ArticleType, List<Map<String, Object>>> entry : Multimaps.asMap(groupedArticles).entrySet()) {
      ArticleType type = entry.getKey();
      TypedArticleGroup group = new TypedArticleGroup(type, entry.getValue());
      articleGroups.add(group);

      log.warn(String.format("Issue %s has articles of type \"%s\", which is not configured for %s: %s",
          issueId, type.getName(), site.getKey(),
          Lists.transform(group.articles, article -> article.get("doi"))));
    }

    return articleGroups;
  }

  private void populateRelatedArticles(Map<String, Object> article) throws IOException {
    Map<String, Object> relationshipMetadata = articleApi.requestObject(
        ApiAddress.builder("articles").embedDoi((String) article.get("doi"))
            .addToken("relationships").build(), Map.class);

    List<Map<String, String>> inbound = (List<Map<String, String>>) relationshipMetadata.get("inbound");
    List<Map<String, String>> outbound = (List<Map<String, String>>) relationshipMetadata.get("outbound");
    List<RelatedArticle> relatedArticles = Stream.concat(inbound.stream(), outbound.stream())
        .map(amendment -> new RelatedArticle(amendment.get("doi"), amendment.get("title"),
            LocalDate.parse(amendment.get("publicationDate"))))
        .distinct()
        .sorted(Comparator.comparing(RelatedArticle::getPublicationDate).reversed())
        .collect(Collectors.toList());

    article.put("relatedArticles", relatedArticles);
  }

  private void populateAuthors(Map<String, Object> article) throws IOException {
    Map<String, ?> solrResult = (Map<String, ?>) solrSearchApi.lookupArticleByDoi((String) article.get("doi"));
    List<SolrArticleAdapter> solrArticles = SolrArticleAdapter.unpackSolrQuery(solrResult);
    article.put("authors", solrArticles.size() > 0 ? solrArticles.get(0).getAuthors() : ImmutableList.of());
  }
}
