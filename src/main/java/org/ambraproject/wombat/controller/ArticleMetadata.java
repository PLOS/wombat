package org.ambraproject.wombat.controller;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.model.ArticleType;
import org.ambraproject.wombat.service.ApiAddress;
import org.ambraproject.wombat.service.ArticleResolutionService;
import org.ambraproject.wombat.service.ArticleService;
import org.ambraproject.wombat.service.ArticleTransformService;
import org.ambraproject.wombat.service.XmlUtil;
import org.ambraproject.wombat.service.remote.ArticleApi;
import org.ambraproject.wombat.service.remote.CorpusContentApi;
import org.ambraproject.wombat.util.TextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ArticleMetadata {
  private static final Logger log = LoggerFactory.getLogger(ArticleMetadata.class);

  private final Factory factory; // for further service access
  private final Site site;
  private final RequestedDoiVersion articleId;
  private final ArticlePointer articlePointer;
  private final Map<String, ?> ingestionMetadata;
  private final Map<String, ?> itemTable;
  private final Map<String, List<Map<String, ?>>> relationships;

  private ArticleMetadata(Factory factory, Site site,
                          RequestedDoiVersion articleId, ArticlePointer articlePointer,
                          Map<String, ?> ingestionMetadata, Map<String, ?> itemTable,
                          Map<String, List<Map<String, ?>>> relationships) {
    this.factory = Objects.requireNonNull(factory);
    this.site = Objects.requireNonNull(site);
    this.articleId = Objects.requireNonNull(articleId);
    this.articlePointer = Objects.requireNonNull(articlePointer);
    this.ingestionMetadata = Collections.unmodifiableMap(ingestionMetadata);
    this.itemTable = Collections.unmodifiableMap(itemTable);
    this.relationships = Collections.unmodifiableMap(relationships);
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
    private RequestMappingContextDictionary requestMappingContextDictionary;

    public ArticleMetadata get(Site site, RequestedDoiVersion id) throws IOException {
      return get(site, id, articleResolutionService.toIngestion(id));
    }

    public ArticleMetadata get(Site site, RequestedDoiVersion id, ArticlePointer articlePointer) throws IOException {
      Map<String, Object> ingestionMetadata = (Map<String, Object>) articleApi.requestObject(
          articlePointer.asApiAddress().build(), Map.class);
      Map<String, ?> itemTable = articleService.getItemTable(articlePointer);
      Map<String, List<Map<String, ?>>> relationships = articleApi.requestObject(
          ApiAddress.builder("articles").embedDoi(articlePointer.getDoi()).addToken("relationships").build(),
          Map.class);

      return new ArticleMetadata(this, site, id, articlePointer, ingestionMetadata, itemTable, relationships);
    }
  }

  public ArticlePointer getArticlePointer() {
    return articlePointer;
  }

  public Map<String, ?> getIngestionMetadata() {
    return ingestionMetadata;
  }

  public ArticleMetadata populate(HttpServletRequest request, Model model) throws IOException {
    model.addAttribute("versionPtr", articlePointer.getVersionParameter());
    model.addAttribute("articlePtr", articlePointer.asParameterMap());

    model.addAttribute("article", ingestionMetadata);

    model.addAttribute("articleItems", itemTable);
    model.addAttribute("figures", getFigureView());

    model.addAttribute("articleType", getArticleType());
    model.addAttribute("commentCount", getCommentCount());
    model.addAttribute("containingLists", getContainingArticleLists());
    model.addAttribute("categoryTerms", getCategoryTerms());
    model.addAttribute("relatedArticles", getRelatedArticles());
    populateAuthors(model);

    model.addAttribute("revisionMenu", getRevisionMenu());

    return this;
  }

  private List<Map<String, ?>> getRevisionMenu() throws IOException {
    List<Map<String, ?>> revisionList = factory.articleApi.requestObject(
        ApiAddress.builder("articles").embedDoi(articleId.getDoi()).addToken("revisions").build(),
        List.class);
    return revisionList.stream()
        .sorted(Comparator.comparing((Map<String, ?> revision) -> ((Number) revision.get("revisionNumber")).intValue()).reversed())
        .collect(Collectors.toList());
  }

  /**
   * Validate that an article ought to be visible to the user. If not, throw an exception indicating that the user
   * should see a 404.
   * <p/>
   * An article may be invisible if it is not in a published state, or if it has not been published in a journal
   * corresponding to the site.
   *
   * @throws NotVisibleException if the article is not visible on the site
   */
  public ArticleMetadata validateVisibility(String handlerName) {
    Map<String, ?> journal = (Map<String, ?>) ingestionMetadata.get("journal");
    String publishedJournalKey = (String) journal.get("journalKey");
    String siteJournalKey = site.getJournalKey();
    if (!publishedJournalKey.equals(siteJournalKey)) {
      Link link = buildCrossSiteRedirect(publishedJournalKey, handlerName);
      throw new InternalRedirectException(link);
    }
    return this;
  }

  private Link buildCrossSiteRedirect(String targetJournal, String handlerName) {
    Site targetSite = this.site.getTheme().resolveForeignJournalKey(factory.siteSet, targetJournal);
    return Link.toForeignSite(site, targetSite)
        .toPattern(factory.requestMappingContextDictionary, handlerName)
        .addQueryParameters(articlePointer.asParameterMap())
        .build();
  }

  private static final ImmutableSet<String> FIGURE_TYPES = ImmutableSet.of("figure", "table");

  /*
   * Build a view of the article's figures and tables, with the following properties that are significant for display:
   *
   *   (1) The figure DOIs are listed in the same order in which they appear in the original manuscript and should be
   *       displayed to the user (in a table of contents, figure carousel, etc.). Compare to the item table, which has
   *       no order.
   *
   *   (2) Only items of the type "figure" or "table" are included. It excludes other items such as the manuscript,
   *       the PDF file, supplementary material, inline graphics, and striking images.
   */
  public List<Map<String, ?>> getFigureView() {
    List<Map<String, ?>> assetsLinkedFromManuscript = (List<Map<String, ?>>) ingestionMetadata.get("assetsLinkedFromManuscript");
    return assetsLinkedFromManuscript.stream()
        .map((Map<String, ?> asset) -> {
          Map<String, ?> item = (Map<String, ?>) itemTable.get((String) asset.get("doi"));
          String type = (String) item.get("itemType");
          if (!FIGURE_TYPES.contains(type)) return null;

          Map<String, Object> view = new HashMap<>(asset);
          view.put("type", type);
          return view;
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  private ArticleType getArticleType() {
    String typeName = (String) ingestionMetadata.get("articleType");
    return ArticleType.getDictionary(site.getTheme()).lookUp(typeName);
  }

  private Map<String, Integer> getCommentCount() throws IOException {
    return factory.articleApi.requestObject(
        ApiAddress.builder("articles").embedDoi(articleId.getDoi()).addToken("comments").addParameter("count").build(),
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

  private static class Category {
    private final String path;
    private final String term;
    private final int weight;

    private Category(Map<String, ?> categoryData) {
      this.path = Objects.requireNonNull((String) categoryData.get("path"));
      this.term = getCategoryTermFromPath(path);
      this.weight = ((Number) categoryData.get("weight")).intValue();
    }

    private static final Splitter CATEGORY_SPLITTER = Splitter.on('/').omitEmptyStrings();

    private static String getCategoryTermFromPath(String path) {
      return Iterables.getLast(CATEGORY_SPLITTER.split(path));
    }

    private String getTerm() {
      return term;
    }

    private int getWeight() {
      return weight;
    }
  }

  /**
   * Iterate over article categories and extract and sort unique category terms (i.e., the final category term in a
   * given category path)
   *
   * @return a sorted list of category terms
   */
  private List<String> getCategoryTerms() throws IOException {
    List<Map<String, ?>> categoryViews = (List<Map<String, ?>>) factory.articleApi.requestObject(
        ApiAddress.builder("articles").embedDoi(articleId.getDoi()).addToken("categories").build(),
        List.class);

    // Remove duplicate paths that have the same term.
    Map<String, Category> categoryMap = Maps.newHashMapWithExpectedSize(categoryViews.size());
    for (Map<String, ?> categoryView : categoryViews) {
      Category category = new Category(categoryView);
      Category previous = categoryMap.put(category.term, category);
      if (previous != null) {
        // They should differ only by path.
        if (category.weight != previous.weight) {
          log.warn(String.format("In category assignments for %s, inconsistent weights for same term. \"%s\": %d; \"%s\": %d",
              articlePointer.getDoi(), category.path, category.weight, previous.path, previous.weight));
        } // else, it's okay for it to be replaced because the term and weight are the same
      }
    }

    // Sort by descending weight, then alphabetically by term
    return categoryMap.values().stream()
        .sorted(Comparator.comparing(Category::getWeight).reversed().thenComparing(Category::getTerm))
        .map(Category::getTerm)
        .collect(Collectors.toList());
  }

  private static boolean isPublished(Map<String, ?> relatedArticle) {
    return relatedArticle.get("revisionNumber") != null;
  }

  private static final Comparator<Map<String, ?>> BY_DESCENDING_PUB_DATE = Comparator
      .comparing((Map<String, ?> articleMetadata) ->
          LocalDate.parse((String) articleMetadata.get("publicationDate")))
      .reversed();

  private static final ImmutableSet<String> RELATIONSHIP_DIRECTIONS = ImmutableSet.of("inbound", "outbound");

  private List<Map<String, ?>> getRelatedArticles() {
    // Eliminate duplicate DOIs (in case there are inbound and outbound relationships with the same article)
    Map<String, Map<String, ?>> relationshipsByDoi = new HashMap<>();
    for (String direction : RELATIONSHIP_DIRECTIONS) {
      for (Map<String, ?> relatedArticle : relationships.get(direction)) {
        String relatedArticleDoi = (String) relatedArticle.get("doi");
        Map<String, ?> previous = relationshipsByDoi.put(relatedArticleDoi, relatedArticle);

        if (previous != null) {
          // Collisions are okay if a relationship exists in each direction. Verify that the data are consistent.
          Preconditions.checkState(Objects.equals(relatedArticle.get("revisionNumber"), previous.get("revisionNumber"))
              && Objects.equals(relatedArticle.get("title"), previous.get("title"))
              && Objects.equals(relatedArticle.get("publicationDate"), previous.get("publicationDate")));
          // It is fine for relatedArticle.get("type") and previous.get("type") to be unequal.
        }
      }
    }

    return relationshipsByDoi.values().stream()
        .filter(ArticleMetadata::isPublished)
        .sorted(BY_DESCENDING_PUB_DATE)
        .collect(Collectors.toList());
  }

  public Map<String, ?> getAuthors() throws IOException {
    ApiAddress authorAddress = articlePointer.asApiAddress().addToken("authors").build();
    return factory.articleApi.requestObject(authorAddress, Map.class);
  }

  /**
   * Appends additional info about article authors to the model.
   *
   * @param model model to be passed to the view
   * @return the list of authors appended to the model
   * @throws IOException
   */
  private void populateAuthors(Model model) throws IOException {
    Map<?, ?> allAuthorsData = getAuthors();
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
        .filter((Map<String, ?> relatedArticle) -> isPublished(relatedArticle) && getAmendmentType(relatedArticle).isPresent())
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
      String body;
      try {
        body = getAmendmentBody(amendmentId);
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
  private String getAmendmentBody(ArticlePointer amendmentId) throws IOException {
    return factory.corpusContentApi.readManuscript(amendmentId, site, "amendmentBody",
        (InputStream stream) -> {
          // Extract the "/article/body" element from the amendment XML, not to be confused with the HTML <body> element.
          String bodyXml = XmlUtil.extractElement(stream, "body");
          return factory.articleTransformService.transformAmendmentBody(site, amendmentId, bodyXml);
        });
  }

}
