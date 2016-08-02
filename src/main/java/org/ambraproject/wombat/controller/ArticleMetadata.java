package org.ambraproject.wombat.controller;

import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import org.ambraproject.wombat.config.RemoteCacheSpace;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.service.ApiAddress;
import org.ambraproject.wombat.service.ArticleResolutionService;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.RenderContext;
import org.ambraproject.wombat.service.XmlService;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.CorpusContentApi;
import org.ambraproject.wombat.util.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;

public class ArticleMetadata {
  private static final Logger log = LoggerFactory.getLogger(ArticleMetadata.class);

  private final Factory factory; // for further service access
  private final Site site;
  private final RequestedDoiVersion articleId;
  private final ArticlePointer articlePointer;
  private final Map<String, ?> ingestionMetadata;
  private final Map<String, List<Map<String, ?>>> relationships;

  private ArticleMetadata(Factory factory, Site site,
                          RequestedDoiVersion articleId, ArticlePointer articlePointer,
                          Map<String, ?> ingestionMetadata,
                          Map<String, List<Map<String, ?>>> relationships) {
    this.factory = Objects.requireNonNull(factory);
    this.site = Objects.requireNonNull(site);
    this.articleId = Objects.requireNonNull(articleId);
    this.articlePointer = Objects.requireNonNull(articlePointer);
    this.ingestionMetadata = Objects.requireNonNull(ingestionMetadata);
    this.relationships = Objects.requireNonNull(relationships);
  }

  public static class Factory {
    @Autowired
    private ArticleApi articleApi;
    @Autowired
    private CorpusContentApi corpusContentApi;
    @Autowired
    private ArticleService articleService;
    @Autowired
    private ArticleResolutionService articleResolutionService;
    @Autowired
    private SiteSet siteSet;
    @Autowired
    private ArticleTransformService articleTransformService;
    @Autowired
    private XmlService xmlService;

    public ArticleMetadata get(Site site, RequestedDoiVersion id) throws IOException {
      ArticlePointer articlePointer = articleResolutionService.toIngestion(id);
      Map<String, Object> ingestionMetadata = (Map<String, Object>) articleApi.requestObject(
          articlePointer.asApiAddress().build(), Map.class);
      Map<String, List<Map<String, ?>>> relationships = articleApi.requestObject(
          ApiAddress.builder("articles").embedDoi(articlePointer.getDoi()).addToken("relationships").build(),
          Map.class);

      return new ArticleMetadata(this, site, id, articlePointer, ingestionMetadata, relationships);
    }
  }

  public Map<String, ?> getIngestionMetadata() {
    return ingestionMetadata;
  }

  public ArticleMetadata populate(HttpServletRequest request, Model model) throws IOException {
    addCrossPublishedJournals(request, model);
    model.addAttribute("article", ingestionMetadata);
    model.addAttribute("articleItems", factory.articleService.getItemTable(articlePointer));
    model.addAttribute("commentCount", getCommentCount());
    model.addAttribute("containingLists", getContainingArticleLists());
    model.addAttribute("categoryTerms", getCategoryTerms());
    model.addAttribute("relatedArticles", getRelatedArticles());
    requestAuthors(model);

    model.addAttribute("revisionMenu", getRevisionList());

    return this;
  }

  private List<Integer> getRevisionList() throws IOException {
    // TODO: Unify with the same API call that we already did in articleService.requestArticleMetadata
    Map<String, ?> articleOverview = factory.articleApi.requestObject(
        ApiAddress.builder("articles").embedDoi(articleId.getDoi()).build(),
        Map.class);
    Map<String, ?> revisionMap = (Map<String, ?>) articleOverview.get("revisions");
    return revisionMap.keySet().stream()
        .map(Integer::valueOf)
        .sorted()
        .collect(Collectors.toList());
  }

  private static final boolean DEBUG_VISIBILITY = true;

  /**
   * Validate that an article ought to be visible to the user. If not, throw an exception indicating that the user
   * should see a 404.
   * <p/>
   * An article may be invisible if it is not in a published state, or if it has not been published in a journal
   * corresponding to the site.
   *
   * @throws NotVisibleException if the article is not visible on the site
   */
  public ArticleMetadata validateVisibility() {
    if (DEBUG_VISIBILITY) return this;

    String state = (String) ingestionMetadata.get("state");
    if (!"published".equals(state)) {
      throw new NotVisibleException("Article is in unpublished state: " + state);
    }

    Set<String> articleJournalKeys = ((Map<String, ?>) ingestionMetadata.get("journals")).keySet();
    String siteJournalKey = site.getJournalKey();
    if (!articleJournalKeys.contains(siteJournalKey) && !DEBUG_VISIBILITY) {
      throw new NotVisibleException("Article is not published in: " + site);
    }

    return this;
  }

  /**
   * Add links to cross-published journals to the model.
   * <p>
   * Each journal in which the article was published (according to the supplied article metadata) will be represented in
   * the model, other than the journal belonging to the site being browsed. If that journal is the only one, nothing is
   * added to the model. The journal of original publication (according to the article metadata's eISSN) is added under
   * the named {@code "originalPub"}, and other journals are added as a collection named {@code "crossPub"}.
   *
   * @param request the contextual request (used to build cross-site links)
   * @param model   the page model into which to insert the link values
   * @throws IOException
   */
  private void addCrossPublishedJournals(HttpServletRequest request, Model model)
      throws IOException {
    final Map<?, ?> publishedJournals = (Map<?, ?>) ingestionMetadata.get("journals");
    if (publishedJournals == null) {
      // TODO: Implement when cross-pub journals are supported in versioned API
      model.addAttribute("crossPub", ImmutableList.of());
      model.addAttribute("originalPub", ImmutableMap.builder()
          .put("href", "TODO").put("title", "TODO").put("italicizeTitle", false).build());
      return;
    }

    final String eissn = (String) ingestionMetadata.get("eIssn");
    Collection<Map<String, ?>> crossPublishedJournals;
    Map<String, ?> originalJournal = null;

    if (publishedJournals.size() <= 1) {
      // The article was published in only one journal.
      // Assume it is the one being browsed (validateArticleVisibility would have caught it otherwise).
      crossPublishedJournals = ImmutableList.of();
    } else {
      crossPublishedJournals = Lists.newArrayListWithCapacity(publishedJournals.size() - 1);
      String localJournal = site.getJournalKey();

      for (Map.Entry<?, ?> journalEntry : publishedJournals.entrySet()) {
        String journalKey = (String) journalEntry.getKey();
        if (journalKey.equals(localJournal)) {
          // This is the journal being browsed right now, so don't add a link
          continue;
        }

        // Make a mutable copy to clobber
        Map<String, Object> crossPublishedJournalMetadata = new HashMap<>((Map<? extends String, ?>) journalEntry.getValue());

        // Find the site object (if possible) for the other journal
        String crossPublishedJournalKey = (String) crossPublishedJournalMetadata.get("journalKey");
        Site crossPublishedSite = site.getTheme().resolveForeignJournalKey(factory.siteSet, crossPublishedJournalKey);

        // Set up an href link to the other site's root page.
        // Do not link to handlerName="homePage" because we don't know if the other site has disabled it.
        String homepageLink = Link.toForeignSite(site, crossPublishedSite).toPath("").get(request);
        crossPublishedJournalMetadata.put("href", homepageLink);

        // Look up whether the other site wants its journal title italicized
        // (This isn't a big deal because it's only one value, but if similar display details pile up
        // in the future, it would be better to abstract them out than to handle them all individually here.)
        boolean italicizeTitle = (boolean) crossPublishedSite.getTheme().getConfigMap("journal").get("italicizeTitle");
        crossPublishedJournalMetadata.put("italicizeTitle", italicizeTitle);

        if (eissn.equals(crossPublishedJournalMetadata.get("eIssn"))) {
          originalJournal = crossPublishedJournalMetadata;
        } else {
          crossPublishedJournals.add(crossPublishedJournalMetadata);
        }
      }
    }

    model.addAttribute("crossPub", crossPublishedJournals);
    model.addAttribute("originalPub", originalJournal);
  }

  private Map<String, Integer> getCommentCount() throws IOException {
    // TODO: Determine actual service; replace this placeholder
    if (true) return ImmutableMap.<String, Integer>builder().put("all", 0).put("root", 0).build();

    return factory.articleApi.requestObject(
        ApiAddress.builder("articles").embedDoi(articleId.getDoi()).addParameter("commentCount").build(),
        Map.class);
  }


  private Map<String, Collection<Object>> getContainingArticleLists() throws IOException {
    List<Map<?, ?>> articleListObjects = factory.articleApi.requestObject(
        ApiAddress.builder("articles").embedDoi(articleId.getDoi()).addParameter("lists").build(),
        List.class);
    Multimap<String, Object> result = LinkedListMultimap.create(articleListObjects.size());
    for (Map<?, ?> articleListObject : articleListObjects) {
      String listType = Preconditions.checkNotNull((String) articleListObject.get("type"));
      result.put(listType, articleListObject);
    }
    return result.asMap();
  }

  /**
   * Iterate over article categories and extract and sort unique category terms (i.e., the final category term in a
   * given category path)
   *
   * @return a sorted list of category terms
   */
  private List<String> getCategoryTerms() {
    List<Map<String, ?>> categories = (List<Map<String, ?>>) ingestionMetadata.get("categories");
    if (categories == null || categories.isEmpty()) {
      return ImmutableList.of();
    }

    // create a map of terms/weights (effectively removes duplicate terms through the mapping)
    Map<String, Double> termsMap = new HashMap<>();
    for (Map<String, ?> category : categories) {
      String[] categoryTerms = ((String) category.get("path")).split("/");
      String categoryTerm = categoryTerms[categoryTerms.length - 1];
      termsMap.put(categoryTerm, (Double) category.get("weight"));
    }

    // use Guava for sorting, first on weight (descending), then on category term
    Comparator valueComparator = Ordering.natural().reverse().onResultOf(Functions.forMap(termsMap)).compound(Ordering.natural());
    SortedMap<String, Double> sortedTermsMap = ImmutableSortedMap.copyOf(termsMap, valueComparator);

    return new ArrayList<>(sortedTermsMap.keySet());

  }

  private static final Comparator<Map<String, ?>> BY_DESCENDING_PUB_DATE = Comparator
      .comparing((Map<String, ?> articleMetadata) ->
          LocalDate.parse((String) articleMetadata.get("publicationDate")))
      .reversed();

  private static final ImmutableSet<String> RELATIONSHIP_DIRECTIONS = ImmutableSet.of("inbound", "outbound");

  private List<Map<String, ?>> getRelatedArticles() {
    // Eliminate duplicate DOIs (in case there is are inbound and outbound relationships with the same article)
    Map<String, Map<String, ?>> relationshipsByDoi = new HashMap<>();
    for (String direction : RELATIONSHIP_DIRECTIONS) {
      for (Map<String, ?> relatedArticle : relationships.get(direction)) {
        // It doesn't matter if this overwrites: we expect the title and date to be the same
        relationshipsByDoi.put((String) relatedArticle.get("doi"), relatedArticle);
      }
    }

    return relationshipsByDoi.values().stream()
        .sorted(BY_DESCENDING_PUB_DATE)
        .collect(Collectors.toList());
  }

  /**
   * Appends additional info about article authors to the model.
   *
   * @param model model to be passed to the view
   * @return the list of authors appended to the model
   * @throws IOException
   */
  private void requestAuthors(Model model) throws IOException {
    ApiAddress authorAddress = articlePointer.asApiAddress().addToken("authors").build();
    Map<?, ?> allAuthorsData = factory.articleApi.requestObject(authorAddress, Map.class);
    List<?> authors = (List<?>) allAuthorsData.get("authors");
    model.addAttribute("authors", authors);

    // Putting this here was a judgement call.  One could make the argument that this logic belongs
    // in Rhino, but it's so simple I elected to keep it here for now.
    List<String> equalContributors = new ArrayList<>();

    for (Object o : authors) {
      Map<String, Object> author = (Map<String, Object>) o;
      String fullName = (String) author.get("fullName");

      Object obj = author.get("equalContrib");
      if (obj != null && (boolean) obj) {
        equalContributors.add(fullName);
      }

      // remove the footnote marker from the current address
      List<String> currentAddresses = (List<String>) author.get("currentAddresses");
      for (ListIterator<String> iterator = currentAddresses.listIterator(); iterator.hasNext(); ) {
        String currentAddress = iterator.next();
        iterator.set(TextUtil.removeFootnoteMarker(currentAddress));
      }
    }

    model.addAttribute("authorContributions", allAuthorsData.get("authorContributions"));
    model.addAttribute("competingInterests", allAuthorsData.get("competingInterests"));
    model.addAttribute("correspondingAuthors", allAuthorsData.get("correspondingAuthorList"));
    model.addAttribute("equalContributors", equalContributors);
  }


  /**
   * Check related articles for ones that amend this article. Set them up for special display, and retrieve additional
   * data about those articles from the service tier.
   */
  public ArticleMetadata fillAmendments(Model model) throws IOException {
    List<Map<String, ?>> inboundRelationships = relationships.get("inbound");
    List<Map<String, Object>> amendments = inboundRelationships.parallelStream()
        .filter((Map<String, ?> relatedArticle) -> getAmendmentType(relatedArticle).isPresent())
        .map((Map<String, ?> relatedArticle) -> createAmendment(site, relatedArticle))
        .sorted(BY_DESCENDING_PUB_DATE)
        .collect(Collectors.toList());
    List<AmendmentGroup> amendmentGroups = buildAmendmentGroups(amendments);
    model.addAttribute("amendments", amendmentGroups);

    return this;
  }

  /**
   * Types of related articles that get special display handling.
   */
  private static enum AmendmentType {
    CORRECTION("corrected-article"),
    EOC("object-of-concern"),
    RETRACTION("retracted-article");

    /**
     * A value of the "type" field of an object in the list of inbound relationships.
     */
    private final String relationshipType;

    private AmendmentType(String relationshipType) {
      this.relationshipType = relationshipType;
    }

    // For use as a key in maps destined for the FreeMarker model
    private String getLabel() {
      return name().toLowerCase();
    }

    private static final ImmutableMap<String, AmendmentType> BY_RELATIONSHIP_TYPE = Maps.uniqueIndex(
        EnumSet.allOf(AmendmentType.class), input -> input.relationshipType);
  }

  /**
   * @return the amendment type of the relationship, or empty if the relationship is not an amendment
   */
  private Optional<AmendmentType> getAmendmentType(Map<String, ?> relatedArticle) {
    String relationshipType = (String) relatedArticle.get("type");
    AmendmentType amendmentType = AmendmentType.BY_RELATIONSHIP_TYPE.get(relationshipType);
    return Optional.ofNullable(amendmentType);
  }

  /**
   * @param site           the site being rendered
   * @param relatedArticle a relationship to an amendment to this article
   * @return a model of the amendment
   * @throws IllegalArgumentException if the relationship is not of an amendment type
   */
  private Map<String, Object> createAmendment(Site site, Map<String, ?> relatedArticle) {
    AmendmentType amendmentType = getAmendmentType(relatedArticle).orElseThrow(IllegalArgumentException::new);

    String doi = (String) relatedArticle.get("doi");

    ArticlePointer amendmentId;
    Map<String, Object> amendment;
    Map<String, ?> authors;
    try {
      amendmentId = factory.articleResolutionService.toIngestion(RequestedDoiVersion.of(doi)); // always uses latest revision
      amendment = (Map<String, Object>) factory.articleApi.requestObject(amendmentId.asApiAddress().build(), Map.class);
      authors = factory.articleApi.requestObject(amendmentId.asApiAddress().addToken("authors").build(), Map.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    amendment.putAll(authors);

    // Display the body only on non-correction amendments. Would be better if this were configurable per theme.
    if (amendmentType != AmendmentType.CORRECTION) {
      RenderContext renderContext = new RenderContext(site,
          RequestedDoiVersion.ofIngestion(amendmentId.getDoi(), amendmentId.getIngestionNumber()));
      String body;
      try {
        body = getAmendmentBody(renderContext);
      } catch (IOException e) {
        throw new RuntimeException("Could not get body for amendment: " + doi, e);
      }
      amendment.put("body", body);
    }

    amendment.put("type", amendmentType.getLabel());
    return amendment;
  }

  /**
   * Combine adjacent amendments that have the same type into one AmendmentGroup object, for display purposes. If
   * multiple amendments share a type but are separated in order by a different type, they go in separate groups.
   *
   * @param amendments a list of amendment objects in their desired display order
   * @return the amendments grouped by type in the same order
   */
  private static List<AmendmentGroup> buildAmendmentGroups(List<Map<String, Object>> amendments) {
    if (amendments.isEmpty()) return ImmutableList.of();

    List<AmendmentGroup> amendmentGroups = new ArrayList<>(amendments.size());
    List<Map<String, Object>> nextGroup = null;
    String type = null;
    for (Map<String, Object> amendment : amendments) {
      String nextType = (String) amendment.get("type");
      if (nextGroup == null || !Objects.equals(type, nextType)) {
        if (nextGroup != null) {
          amendmentGroups.add(new AmendmentGroup(type, nextGroup));
        }
        type = nextType;
        nextGroup = new ArrayList<>();
      }
      nextGroup.add(amendment);
    }
    amendmentGroups.add(new AmendmentGroup(type, nextGroup));
    return amendmentGroups;
  }

  public static class AmendmentGroup {
    private final String type;
    private final ImmutableList<Map<String, Object>> amendments;

    private AmendmentGroup(String type, List<Map<String, Object>> amendments) {
      this.type = Objects.requireNonNull(type);
      this.amendments = ImmutableList.copyOf(amendments);
      Preconditions.checkArgument(!this.amendments.isEmpty());
    }

    public String getType() {
      return type;
    }

    public ImmutableList<Map<String, Object>> getAmendments() {
      return amendments;
    }
  }

  /**
   * Retrieve and transform the body of an amendment article from its XML file. The returned value is cached.
   *
   * @return the body of the amendment article, transformed into HTML for display in a notice on the amended article
   */
  private String getAmendmentBody(final RenderContext renderContext) throws IOException {
    return factory.corpusContentApi.readManuscript(renderContext, RemoteCacheSpace.AMENDMENT_BODY,
        (InputStream stream) -> {
          // Extract the "/article/body" element from the amendment XML, not to be confused with the HTML <body> element.
          String bodyXml = factory.xmlService.extractElement(stream, "body");
          try {
            return factory.articleTransformService.transformExcerpt(renderContext, bodyXml, null);
          } catch (TransformerException e) {
            throw new RuntimeException(e);
          }
        });
  }

}
