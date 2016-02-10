package org.ambraproject.wombat.rss;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.rss.Description;
import com.rometools.rome.feed.rss.Item;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.url.Link;
import org.springframework.beans.factory.annotation.Autowired;
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

@SuppressWarnings("unchecked")
public class WombatRssViewer extends AbstractRssFeedView {

  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;

  @Override
  protected List<Item> buildFeedItems(Map<String, Object> model,
      HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
    Site site = (Site) model.get("site");
    ItemFactory itemFactory = new ItemFactory(httpServletRequest, site);
    List<Map<String, ?>> solrResults = (List<Map<String, ?>>) model.get("solrResults");
    return solrResults.stream().map(itemFactory::represent).collect(Collectors.toList());
  }

  private class ItemFactory {
    private final HttpServletRequest request;
    private final Site site;

    private ItemFactory(HttpServletRequest request, Site site) {
      this.request = Objects.requireNonNull(request);
      this.site = Objects.requireNonNull(site);
    }

    public Item represent(Map<String, ?> article) {
      Item item = new Item();
      item.setTitle((String) article.get("title"));
      item.setLink(Link.toAbsoluteAddress(site)
          .toPattern(requestMappingContextDictionary, "article")
          .addQueryParameter("id", article.get("id"))
          .build().get(request));

      item.setPubDate(getPubDate(article));

      ArrayList<String> authorList = (ArrayList<String>) article.get("author_display");
      if (authorList != null) {
        item.setAuthor(Joiner.on(", ").join(authorList));
      }
      item.setDescription(buildDescription(article));

      return item;
    }

    private Date getPubDate(Map<String, ?> article) {
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

    //both "abstract" and "abstract_primary_display" are single-item lists
    private Description buildDescription(Map<String, ?> article) {
      Description description = new Description();
      String descriptionString = ((ArrayList<String>) article.get("abstract_primary_display")).get(0);
      if (Strings.isNullOrEmpty(descriptionString)) {
        descriptionString = ((ArrayList<String>) article.get("abstract")).get(0);
      }
      description.setValue(descriptionString);
      return description;
    }
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
