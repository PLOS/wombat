package org.ambraproject.wombat.rss;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.rometools.rome.feed.atom.Content;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.feed.atom.Person;
import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.rss.Description;
import com.rometools.rome.feed.rss.Image;
import com.rometools.rome.feed.rss.Item;
import com.rometools.rome.feed.synd.SyndPerson;
import com.rometools.rome.feed.synd.SyndPersonImpl;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.url.Link;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.feed.AbstractAtomFeedView;
import org.springframework.web.servlet.view.feed.AbstractRssFeedView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ArticleFeedView {

  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;

  private final View articleRssView = new ArticleRssView();
  private final View articleAtomView = new ArticleAtomView();

  /**
   * @return a Spring view that represents articles from the model as an RSS feed
   */
  public View getArticleRssView() {
    return articleRssView;
  }

  /**
   * @return a Spring view that represents articles from the model as an Atom feed
   */
  public View getArticleAtomView() {
    return articleAtomView;
  }

  private class ArticleRssView extends AbstractRssFeedView {
    @Override
    protected List<Item> buildFeedItems(Map<String, Object> model,
                                        HttpServletRequest request, HttpServletResponse response) {
      return buildElements(model, request, RssFactory::new);
    }

    @Override
    protected void buildFeedMetadata(Map<String, Object> model, Channel feed, HttpServletRequest request) {
      FeedMetadata feedMetadata = new FeedMetadata(model, request);

      String feedTitle = feedMetadata.getTitle();
      String feedLink = feedMetadata.getLink();
      feed.setTitle(feedTitle);
      feed.setLink(feedLink);

      feed.setDescription(feedMetadata.getDescription());
      feed.setWebMaster(feedMetadata.getAuthorEmail());
      feed.setLastBuildDate(feedMetadata.getTimestamp());

      String copyright = feedMetadata.getCopyright();
      if (!Strings.isNullOrEmpty(copyright)) {
        feed.setCopyright(copyright);
      }

      String imageLink = feedMetadata.getImageLink();
      if (!Strings.isNullOrEmpty(imageLink)) {
        Image image = new Image();
        image.setUrl(imageLink);

        // Per https://validator.w3.org/feed/docs/rss2.html
        //   "Note, in practice the image <title> and <link> should have the same value as the channel's <title> and <link>."
        image.setTitle(feedTitle);
        image.setLink(feedLink);

        feed.setImage(image);
      }

      feed.setDocs("https://validator.w3.org/feed/docs/rss2.html");
    }
  }

  private class ArticleAtomView extends AbstractAtomFeedView {
    @Override
    protected List<Entry> buildFeedEntries(Map<String, Object> model,
                                           HttpServletRequest request, HttpServletResponse response) {
      return buildElements(model, request, AtomFactory::new);
    }

    @Override
    protected void buildFeedMetadata(Map<String, Object> model, Feed feed, HttpServletRequest request) {
      FeedMetadata feedMetadata = new FeedMetadata(model, request);

      feed.setId(feedMetadata.getId());
      feed.setTitle(feedMetadata.getTitle());
      feed.setUpdated(feedMetadata.getTimestamp());

      Content subtitle = new Content();
      subtitle.setType("text");
      subtitle.setValue(feedMetadata.getDescription());
      feed.setSubtitle(subtitle);

      com.rometools.rome.feed.atom.Link link = new com.rometools.rome.feed.atom.Link();
      link.setHref(feedMetadata.getLink());
      feed.setAlternateLinks(ImmutableList.of(link));

      feed.setAuthors(ImmutableList.of(buildFeedAuthor(feedMetadata)));

      String imageLink = feedMetadata.getImageLink();
      if (!Strings.isNullOrEmpty(imageLink)) {
        // We are ignoring part of the spec here. From http://atomenabled.org/developers/syndication/
        //   "icon: ...a small image... Icons should be square."
        //   "logo: ...a larger image... Images should be twice as wide as they are tall.
        feed.setIcon(imageLink);
        feed.setLogo(imageLink);
      }

      String copyright = feedMetadata.getCopyright();
      if (!Strings.isNullOrEmpty(copyright)) {
        feed.setRights(copyright);
      }
    }

    private SyndPerson buildFeedAuthor(FeedMetadata feedMetadata) {
      SyndPerson feedAuthor = new SyndPersonImpl();
      feedAuthor.setUri(feedMetadata.getSiteLink());

      String authorName = feedMetadata.getAuthorName();
      if (!Strings.isNullOrEmpty(authorName)) {
        feedAuthor.setName(authorName);
      }

      String authorEmail = feedMetadata.getAuthorEmail();
      if (!Strings.isNullOrEmpty(authorEmail)) {
        feedAuthor.setEmail(authorEmail);
      }

      return feedAuthor;
    }
  }


  /**
   * Values passed from the Spring model to describe the feed.
   */
  public static enum FeedMetadataField {
    ID, TIMESTAMP, TITLE, DESCRIPTION, LINK, IMAGE;

    private String getKey() {
      return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name());
    }

    public Object putInto(Map<String, Object> model, Object value) {
      return model.put(getKey(), value);
    }

    public Object getFrom(Map<String, Object> model) {
      return model.get(getKey());
    }
  }

  private class FeedMetadata {
    private final Map<String, Object> model;
    private final HttpServletRequest request;
    private final Site site;
    private final Map<String, Object> feedConfig;

    private FeedMetadata(Map<String, Object> model, HttpServletRequest request) {
      this.model = Objects.requireNonNull(model);
      this.request = Objects.requireNonNull(request);
      this.site = Objects.requireNonNull((Site) model.get("site"));

      try {
        this.feedConfig = Objects.requireNonNull(site.getTheme().getConfigMap("feed"));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    /**
     * @return a URL that <em>uniquely</em> identifies the feed
     */
    public String getId() {
      String id = (String) FeedMetadataField.ID.getFrom(model);
      return Strings.isNullOrEmpty(id) ? request.getRequestURL().toString() : id;
    }

    /**
     * @return a timestamp for the latest time the feed was updated, defaulting to now
     */
    public Date getTimestamp() {
      Date timestamp = (Date) FeedMetadataField.TIMESTAMP.getFrom(model);
      return (timestamp == null) ? new Date() : timestamp;
    }

    public String getTitle() {
      String journalName = site.getJournalName();
      String feedTitle = (String) FeedMetadataField.TITLE.getFrom(model);
      return Strings.isNullOrEmpty(feedTitle) ? journalName : journalName + ": " + feedTitle;
    }

    public String getDescription() {
      String feedDescription = (String) FeedMetadataField.DESCRIPTION.getFrom(model);
      if (!Strings.isNullOrEmpty(feedDescription)) {
        return feedDescription;
      }
      String siteDescription = (String) feedConfig.get("description");
      return Strings.nullToEmpty(siteDescription);
    }

    public String getSiteLink() {
      return Link.toAbsoluteAddress(site).toPath("").get(request);
    }

    public String getLink() {
      String feedLink = (String) FeedMetadataField.LINK.getFrom(model);
      return Strings.isNullOrEmpty(feedLink) ? getSiteLink() : feedLink;
    }

    public String getCopyright() {
      return (String) feedConfig.get("copyright");
    }

    public String getImageLink() {
      // An image defined for this particular feed takes precedence
      String imagePath = (String) FeedMetadataField.IMAGE.getFrom(model);
      if (Strings.isNullOrEmpty(imagePath)) {
        // Else, check if the site has configured a global image for all feeds
        imagePath = (String) feedConfig.get("image");
      }

      // If we have a path, resolve it into a link
      return (imagePath == null) ? null
          : Link.toAbsoluteAddress(site).toPath(imagePath).get(request);
    }

    public String getAuthorName() {
      return (String) feedConfig.get("authorName");
    }

    public String getAuthorEmail() {
      return (String) feedConfig.get("authorEmail");
    }
  }


  // Function that an AbstractRssFeedView uses to initialize a ElementFactory
  @FunctionalInterface
  private static interface ElementFactoryConstructor<T, F extends ElementFactory<T>> {
    F construct(HttpServletRequest request, Site site);
  }

  // Dispatch from an AbstractRssFeedView to a ElementFactory
  private static <T, F extends ElementFactory<T>> List<T> buildElements(
      Map<String, Object> model, HttpServletRequest request,
      ElementFactoryConstructor<T, F> factoryConstructor) {
    Site site = (Site) model.get("site");
    List<Map<String, ?>> solrResults = (List<Map<String, ?>>) model.get("solrResults");
    F elementFactory = factoryConstructor.construct(request, site);
    return solrResults.stream().map(elementFactory::buildFeedElement).collect(Collectors.toList());
  }


  /**
   * A factory object that represents articles as elements within a feed.
   *
   * @param <T> the type of feed object to output
   */
  private abstract class ElementFactory<T> {
    protected final HttpServletRequest request;
    protected final Site site;
    protected final Map<String, Object> feedConfig;

    private ElementFactory(HttpServletRequest request, Site site) {
      this.request = Objects.requireNonNull(request);
      this.site = Objects.requireNonNull(site);

      try {
        this.feedConfig = Objects.requireNonNull(site.getTheme().getConfigMap("feed"));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    public abstract T buildFeedElement(Map<String, ?> article);

    protected final String getArticleLink(Map<String, ?> article) {
      return Link.toAbsoluteAddress(site)
          .toPattern(requestMappingContextDictionary, "article")
          .addQueryParameter("id", article.get("id"))
          .build().get(request);
    }

    protected final Date getPubDate(Map<String, ?> article) {
      SimpleDateFormat solrDateFormat = new SimpleDateFormat("YYYY-MM-dd", Locale.ENGLISH);
      String publicationDateStr = (String) article.get("publication_date");
      Date pubDate;
      try {
        pubDate = solrDateFormat.parse(publicationDateStr);
      } catch (ParseException e) {
        throw new RuntimeException(e);
      }
      Calendar cal = Calendar.getInstance();
      cal.setTime(pubDate);
      getPublicationTime().ifPresent((LocalTime publicationTime) -> {
        cal.set(Calendar.HOUR_OF_DAY, publicationTime.getHour());
        cal.set(Calendar.MINUTE, publicationTime.getMinute());
        cal.set(Calendar.SECOND, publicationTime.getSecond());
      });
      return cal.getTime();
    }

    private Optional<LocalTime> getPublicationTime() {
      List<Number> publicationTime = (List<Number>) feedConfig.get("publicationTimeOfDay");
      if (publicationTime == null) return Optional.empty();
      if (publicationTime.size() != 2) throw new RuntimeException();
      return Optional.of(LocalTime.of(publicationTime.get(0).intValue(), publicationTime.get(1).intValue()));
    }

    protected final String getAbstractText(Map<String, ?> article) {
      String abstractText = Iterables.getOnlyElement((List<String>) article.get("abstract_primary_display"));
      if (Strings.isNullOrEmpty(abstractText)) {
        abstractText = Iterables.getOnlyElement((List<String>) article.get("abstract"));
      }
      return abstractText;
    }
  }

  private class RssFactory extends ElementFactory<Item> {
    public RssFactory(HttpServletRequest request, Site site) {
      super(request, site);
    }

    /**
     * Represent an article as an RSS feed item.
     *
     * @param article a Solr result of an article
     */
    @Override
    public Item buildFeedElement(Map<String, ?> article) {
      Item item = new Item();
      item.setTitle((String) article.get("title"));
      item.setLink(getArticleLink(article));

      item.setPubDate(getPubDate(article));

      List<String> authorList = (List<String>) article.get("author_display");
      if (authorList != null) {
        item.setAuthor(Joiner.on(", ").join(authorList));
      }
      item.setDescription(buildDescription(article));

      return item;
    }

    private Description buildDescription(Map<String, ?> article) {
      Description description = new Description();
      description.setValue(getAbstractText(article));
      return description;
    }
  }

  private class AtomFactory extends ElementFactory<Entry> {
    public AtomFactory(HttpServletRequest request, Site site) {
      super(request, site);
    }

    /**
     * Represent an article as an Atom feed entry.
     *
     * @param article a Solr result of an article
     */
    @Override
    public Entry buildFeedElement(Map<String, ?> article) {
      Entry entry = new Entry();
      entry.setTitle((String) article.get("title"));

      entry.setAlternateLinks(buildLinks(article));
      entry.setPublished(getPubDate(article));

      List<String> authorList = (List<String>) article.get("author_display");
      if (authorList != null) {
        ArrayList<SyndPerson> authors = new ArrayList<>();
        for (String s : authorList) {
          Person author = new Person();
          author.setName(s);
          authors.add(author);
        }
        entry.setAuthors(authors);
      }
      entry.setContents(buildContents(article));

      return entry;
    }

    private ImmutableList<com.rometools.rome.feed.atom.Link> buildLinks(Map<String, ?> article) {
      String articleId = (String) article.get("id");
      String title = (String) article.get("title");

      com.rometools.rome.feed.atom.Link articleLink = createLink(getArticleLink(article),
          title, Optional.empty(), Optional.empty());
      com.rometools.rome.feed.atom.Link pdfLink = createLink(Link.toAbsoluteAddress(site)
              .toPattern(requestMappingContextDictionary, "asset")
              .addQueryParameter("id", articleId + ".PDF")
              .build().get(request),
          "(PDF) " + title, Optional.of("related"), Optional.of("application/pdf"));
      com.rometools.rome.feed.atom.Link xmlLink = createLink(Link.toAbsoluteAddress(site)
              .toPattern(requestMappingContextDictionary, "asset")
              .addQueryParameter("id", articleId + ".XML")
              .build().get(request),
          "(XML) " + title, Optional.of("related"), Optional.of("text/xml"));

      return ImmutableList.of(articleLink, pdfLink, xmlLink);
    }

    private com.rometools.rome.feed.atom.Link createLink(String href, String title,
                                                         Optional<String> rel, Optional<String> mimetype) {
      com.rometools.rome.feed.atom.Link link = new com.rometools.rome.feed.atom.Link();
      link.setHref(href);
      link.setTitle(title);
      rel.ifPresent(link::setRel);
      mimetype.ifPresent(link::setType);
      return link;
    }

    private List<Content> buildContents(Map<String, ?> article) {
      Content content = new Content();
      content.setType("html");
      content.setValue(getAbstractText(article));
      return ImmutableList.of(content);
    }
  }

}
