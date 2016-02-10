package org.ambraproject.wombat.rss;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.rometools.rome.feed.atom.Content;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.atom.Person;
import com.rometools.rome.feed.synd.SyndPerson;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.view.feed.AbstractAtomFeedView;

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
public class ArticleAtomViewer extends AbstractAtomFeedView {

  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;

  @Override
  protected List<Entry> buildFeedEntries(Map<String, Object> model, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    Site site = (Site) model.get("site");
    EntryFactory entryFactory = new EntryFactory(request, site);
    List<Map<String, ?>> solrResults = (List<Map<String, ?>>) model.get("solrResults");
    return solrResults.stream().map(entryFactory::represent).collect(Collectors.toList());
  }

  private class EntryFactory {
    private final HttpServletRequest request;
    private final Site site;

    private EntryFactory(HttpServletRequest request, Site site) {
      this.request = Objects.requireNonNull(request);
      this.site = Objects.requireNonNull(site);
    }

    public Entry represent(Map<String, ?> article) {
      Entry entry = new Entry();
      entry.setTitle((String) article.get("title"));

      Link link = new Link();
      String linkStr = org.ambraproject.wombat.config.site.url.Link.toAbsoluteAddress(site)
          .toPattern(requestMappingContextDictionary, "article")
          .addQueryParameter("id", article.get("id"))
          .build().get(request);
      link.setHref(linkStr);
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
    private List<Content> buildContents(Map<String, ?> article) {
      Content content = new Content();
      content.setType("html");
      String descriptionString = Iterables.getOnlyElement((ArrayList<String>) article.get("abstract_primary_display"));
      if (Strings.isNullOrEmpty(descriptionString)) {
        descriptionString = Iterables.getOnlyElement((ArrayList<String>) article.get("abstract"));
      }
      content.setValue(descriptionString);
      return ImmutableList.of(content);
    }
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
