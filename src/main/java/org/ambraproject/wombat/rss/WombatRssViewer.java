package org.ambraproject.wombat.rss;

import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.rss.Content;
import com.rometools.rome.feed.rss.Item;
import org.springframework.web.servlet.view.feed.AbstractRssFeedView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WombatRssViewer extends AbstractRssFeedView {

  @Override
  protected List<Item> buildFeedItems(Map<String, Object> model,
      HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {

    List<Item> items = new ArrayList<>();
    Object ob = model.get("feeds");
    if (ob instanceof List){
      for(int i = 0; i < ((List<?>)ob).size(); i++){
        Object feedObj = ((List<?>) ob).get(i);
        WombatFeed myFeed = (WombatFeed)feedObj;
        Item item = new Item();
        item.setTitle(myFeed.getTitle());
        item.setLink(myFeed.getLink());
        item.setPubDate(myFeed.getPubDate());
        Content content = new Content();
        content.setValue(myFeed.getDescription());
        item.setContent(content);
        items.add(item);
      }
    }
    return items;

  }

  @Override
  protected void buildFeedMetadata(Map<String, Object> model, Channel feed, HttpServletRequest request) {
    feed.setTitle("test title");
    feed.setLink("test link");
    feed.setDescription("test desc");
  }
}
