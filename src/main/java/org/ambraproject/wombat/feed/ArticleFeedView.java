package org.ambraproject.wombat.feed;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.rometools.rome.feed.atom.Content;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Person;
import com.rometools.rome.feed.rss.Description;
import com.rometools.rome.feed.rss.Guid;
import com.rometools.rome.feed.rss.Item;
import com.rometools.rome.feed.synd.SyndPerson;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class ArticleFeedView extends AbstractFeedView<Map<String, Object>> {

  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;

  private String getArticleLink(FeedMetadata feedMetadata, Map<String, ?> article) {
    return feedMetadata.buildLink(link -> link
        .toPattern(requestMappingContextDictionary, "article")
        .addQueryParameter("id", article.get("id"))
        .build());
  }

  private Date getPubDate(FeedMetadata feedMetadata, Map<String, ?> article) {
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
    getPublicationTime(feedMetadata).ifPresent((LocalTime publicationTime) -> {
      cal.set(Calendar.HOUR_OF_DAY, publicationTime.getHour());
      cal.set(Calendar.MINUTE, publicationTime.getMinute());
      cal.set(Calendar.SECOND, publicationTime.getSecond());
    });
    return cal.getTime();
  }

  private Optional<LocalTime> getPublicationTime(FeedMetadata feedMetadata) {
    List<Number> publicationTime = (List<Number>) feedMetadata.getFromFeedConfig("publicationTimeOfDay");
    if (publicationTime == null) return Optional.empty();
    if (publicationTime.size() != 2) throw new RuntimeException();
    return Optional.of(LocalTime.of(publicationTime.get(0).intValue(), publicationTime.get(1).intValue()));
  }

  private String getAbstractText(Map<String, ?> article) {
    String abstractText = Iterables.getOnlyElement((List<String>) article.get("abstract_primary_display"));
    if (Strings.isNullOrEmpty(abstractText)) {
      abstractText = Iterables.getOnlyElement((List<String>) article.get("abstract"));
    }
    return abstractText;
  }


  @Override
  protected Item createRssItem(FeedMetadata feedMetadata, Map<String, Object> article) {
    Item item = new Item();
    item.setTitle((String) article.get("title"));
    item.setLink(getArticleLink(feedMetadata, article));

    Guid guid = new Guid();
    guid.setValue((String) article.get("id"));
    guid.setPermaLink(false);
    item.setGuid(guid);

    item.setPubDate(getPubDate(feedMetadata, article));

    List<String> authorList = (List<String>) article.get("author_display");
    if (authorList != null) {
      item.setAuthor(Joiner.on(", ").join(authorList));
    }
    item.setDescription(buildRssDescription(article));

    return item;
  }

  private Description buildRssDescription(Map<String, ?> article) {
    Description description = new Description();
    description.setValue(getAbstractText(article));
    return description;
  }


  @Override
  protected Entry createAtomEntry(FeedMetadata feedMetadata, Map<String, Object> article) {
    Entry entry = new Entry();
    entry.setTitle((String) article.get("title"));
    entry.setId((String) article.get("id"));

    entry.setAlternateLinks(buildLinks(feedMetadata, article));

    Date pubDate = getPubDate(feedMetadata, article);
    entry.setPublished(pubDate);
    entry.setUpdated(pubDate);

    List<String> authorList = (List<String>) article.get("author_display");
    if (authorList != null) {
      List<SyndPerson> authors = new ArrayList<>();
      for (String s : authorList) {
        Person author = new Person();
        author.setName(s);
        authors.add(author);
      }
      entry.setAuthors(authors);
    }
    entry.setContents(buildAtomContents(article));

    return entry;
  }

  private ImmutableList<com.rometools.rome.feed.atom.Link> buildLinks(
      FeedMetadata feedMetadata, Map<String, ?> article) {
    String articleId = (String) article.get("id");
    String title = (String) article.get("title");

    com.rometools.rome.feed.atom.Link articleLink = createAtomLink(getArticleLink(feedMetadata, article),
        title, Optional.empty(), Optional.empty());
    com.rometools.rome.feed.atom.Link pdfLink = createAtomLink(feedMetadata.buildLink(link -> link
            .toPattern(requestMappingContextDictionary, "asset")
            .addQueryParameter("id", articleId + ".PDF")
            .build()),
        "(PDF) " + title, Optional.of("related"), Optional.of("application/pdf"));
    com.rometools.rome.feed.atom.Link xmlLink = createAtomLink(feedMetadata.buildLink(link -> link
            .toPattern(requestMappingContextDictionary, "asset")
            .addQueryParameter("id", articleId + ".XML")
            .build()),
        "(XML) " + title, Optional.of("related"), Optional.of("text/xml"));

    return ImmutableList.of(articleLink, pdfLink, xmlLink);
  }

  private com.rometools.rome.feed.atom.Link createAtomLink(String href, String title,
                                                           Optional<String> rel, Optional<String> mimetype) {
    com.rometools.rome.feed.atom.Link link = new com.rometools.rome.feed.atom.Link();
    link.setHref(href);
    link.setTitle(title);
    rel.ifPresent(link::setRel);
    mimetype.ifPresent(link::setType);
    return link;
  }

  private List<Content> buildAtomContents(Map<String, ?> article) {
    Content content = new Content();
    content.setType("html");
    content.setValue(getAbstractText(article));
    return ImmutableList.of(content);
  }

}
