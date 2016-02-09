package org.ambraproject.wombat.rss;

  import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
public class WombatFeed {
  private String feedId;
  private String title;
  private String description;
  private Date pubDate;
  private String link;
  
  public String getFeedId() {
    return feedId;
  }
  public void setFeedId(String feedId) {
    this.feedId = feedId;
  }
  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public Date getPubDate() {
    return pubDate;
  }
  public void setPubDate(Date pubDate) {
    this.pubDate = pubDate;
  }
  public String getLink() {
    return link;
  }
  public void setLink(String link) {
    this.link = link;
  }
  public List<WombatFeed> createFeed() {
    List<WombatFeed> feeds = new ArrayList<>();
    WombatFeed feed1 = new WombatFeed();
    feed1.setFeedId("100");
    feed1.setTitle("Title one");
    feed1.setDescription("This is description one");
    feed1.setLink("http://www.urlone.com");
    feed1.setPubDate(new Date());
    feeds.add(feed1);
    WombatFeed feed2 = new WombatFeed();
    feed2.setFeedId("200");
    feed2.setTitle("Title two");
    feed2.setDescription("This is description two");
    feed2.setLink("http://www.urltwo.com");
    feed2.setPubDate(new Date());
    feeds.add(feed2);
    return feeds;
  }
}
