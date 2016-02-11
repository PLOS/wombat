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
import com.rometools.rome.feed.rss.Item;
import com.rometools.rome.feed.synd.SyndPerson;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
      feed.setTitle(feedMetadata.getTitle());
      feed.setDescription(feedMetadata.getDescription());
      feed.setLink(feedMetadata.getLink());

      String copyright = feedMetadata.getCopyright();
      if (!Strings.isNullOrEmpty(copyright)) {
        feed.setCopyright(copyright);
      }
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
      feed.setTitle(feedMetadata.getTitle());

      Content subtitle = new Content();
      subtitle.setType("text");
      subtitle.setValue(feedMetadata.getDescription());
      feed.setSubtitle(subtitle);

      com.rometools.rome.feed.atom.Link link = new com.rometools.rome.feed.atom.Link();
      link.setHref(feedMetadata.getLink());
      feed.setAlternateLinks(ImmutableList.of(link));

      String copyright = feedMetadata.getCopyright();
      if (!Strings.isNullOrEmpty(copyright)) {
        feed.setRights(copyright);
      }
    }
  }


  public static enum FeedMetadataField {
    TITLE, DESCRIPTION, LINK;

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

    public String getLink() {
      return Link.toAbsoluteAddress(site).toPath("").get(request);
    }

    public String getCopyright() {
      return (String) feedConfig.get("copyright");
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

    private ElementFactory(HttpServletRequest request, Site site) {
      this.request = Objects.requireNonNull(request);
      this.site = Objects.requireNonNull(site);
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
      cal.set(Calendar.HOUR_OF_DAY, 14); //Publish time is 2PM
      return cal.getTime();
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

      com.rometools.rome.feed.atom.Link link = new com.rometools.rome.feed.atom.Link();
      link.setHref(getArticleLink(article));
      entry.setAlternateLinks(ImmutableList.of(link));

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

    private List<Content> buildContents(Map<String, ?> article) {
      Content content = new Content();
      content.setType("html");
      content.setValue(getAbstractText(article));
      return ImmutableList.of(content);
    }
  }

}
