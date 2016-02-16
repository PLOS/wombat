package org.ambraproject.wombat.feed;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.rometools.rome.feed.atom.Content;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.atom.Person;
import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.rss.Image;
import com.rometools.rome.feed.rss.Item;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.feed.AbstractAtomFeedView;
import org.springframework.web.servlet.view.feed.AbstractRssFeedView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

abstract class AbstractFeedView<I> {

  public final View getRssView() {
    return rssView;
  }

  public final View getAtomView() {
    return atomView;
  }

  protected abstract Item createRssItem(FeedMetadata feedMetadata, I input);

  protected abstract Entry createAtomEntry(FeedMetadata feedMetadata, I input);


  private final View rssView = new AbstractRssFeedView() {
    @Override
    protected List<Item> buildFeedItems(Map<String, Object> model,
                                        HttpServletRequest request, HttpServletResponse response) {
      List<I> inputList = (List<I>) FeedMetadataField.FEED_INPUT.getFrom(model);
      FeedMetadata feedMetadata = new FeedMetadata(model, request);
      return inputList.stream()
          .map((I inputElement) -> createRssItem(feedMetadata, inputElement))
          .collect(Collectors.toList());
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
  };

  private final View atomView = new AbstractAtomFeedView() {
    @Override
    protected List<Entry> buildFeedEntries(Map<String, Object> model,
                                           HttpServletRequest request, HttpServletResponse response) {
      List<I> inputList = (List<I>) FeedMetadataField.FEED_INPUT.getFrom(model);
      FeedMetadata feedMetadata = new FeedMetadata(model, request);
      return inputList.stream()
          .map((I inputElement) -> createAtomEntry(feedMetadata, inputElement))
          .collect(Collectors.toList());
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

      Link link = new Link();
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

    private Person buildFeedAuthor(FeedMetadata feedMetadata) {
      Person feedAuthor = new Person();
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
  };

}
