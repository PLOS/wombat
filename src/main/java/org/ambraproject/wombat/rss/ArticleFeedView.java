package org.ambraproject.wombat.rss;

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
      return buildRepresentations(model, request, RssFactory::new);
    }

    @Override
    protected void buildFeedMetadata(Map<String, Object> model, Channel feed, HttpServletRequest request) {
      Site site = (Site) model.get("site");
      Map<String, Object> feedConfig;
      try {
        feedConfig = site.getTheme().getConfigMap("feed");
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      // TODO: Are these always the same, or should they differ from feed to feed?
      // Maybe a user who is subscribed to several of our feeds would like these fields to help tell the feeds apart.
      feed.setTitle(site.getJournalName());
      feed.setDescription(Strings.nullToEmpty((String) feedConfig.get("description")));

      // TODO: Is this supposed to go to root, or to the feed URL?
      feed.setLink(Link.toAbsoluteAddress(site).toPath("").get(request));
    }
  }

  private class ArticleAtomView extends AbstractAtomFeedView {
    @Override
    protected List<Entry> buildFeedEntries(Map<String, Object> model,
                                           HttpServletRequest request, HttpServletResponse response) {
      return buildRepresentations(model, request, AtomFactory::new);
    }

    @Override
    protected void buildFeedMetadata(Map<String, Object> model, Feed feed, HttpServletRequest request) {
      Site site = (Site) model.get("site");
      Map<String, Object> feedConfig;
      try {
        feedConfig = site.getTheme().getConfigMap("feed");
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      // TODO: set all appropriate feed member variables
    }
  }


  // Function that an AbstractRssFeedView uses to initialize a RepresentationFactory
  @FunctionalInterface
  private static interface RepresentationFactoryConstructor<T, F extends RepresentationFactory<T>> {
    F construct(HttpServletRequest request, Site site);
  }

  // Dispatch from an AbstractRssFeedView to a RepresentationFactory
  private static <T, F extends RepresentationFactory<T>> List<T> buildRepresentations(
      Map<String, Object> model, HttpServletRequest request,
      RepresentationFactoryConstructor<T, F> factoryConstructor) {
    Site site = (Site) model.get("site");
    List<Map<String, ?>> solrResults = (List<Map<String, ?>>) model.get("solrResults");
    F representationFactory = factoryConstructor.construct(request, site);
    return solrResults.stream().map(representationFactory::represent).collect(Collectors.toList());
  }


  /**
   * A factory object that represents articles as feed objects.
   *
   * @param <T> the type of feed object to output
   */
  private abstract class RepresentationFactory<T> {
    protected final HttpServletRequest request;
    protected final Site site;

    private RepresentationFactory(HttpServletRequest request, Site site) {
      this.request = Objects.requireNonNull(request);
      this.site = Objects.requireNonNull(site);
    }

    public abstract T represent(Map<String, ?> article);

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

  private class RssFactory extends RepresentationFactory<Item> {
    public RssFactory(HttpServletRequest request, Site site) {
      super(request, site);
    }

    /**
     * Represent an article as an RSS feed item.
     *
     * @param article a Solr result of an article
     */
    @Override
    public Item represent(Map<String, ?> article) {
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

  private class AtomFactory extends RepresentationFactory<Entry> {
    public AtomFactory(HttpServletRequest request, Site site) {
      super(request, site);
    }

    /**
     * Represent an article as an Atom feed entry.
     *
     * @param article a Solr result of an article
     */
    @Override
    public Entry represent(Map<String, ?> article) {
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
