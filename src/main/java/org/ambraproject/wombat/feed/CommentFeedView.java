package org.ambraproject.wombat.feed;

import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.rss.Item;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class CommentFeedView extends AbstractFeedView<Map<String, Object>> {

  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;

  @Override
  protected Item createRssItem(FeedMetadata feedMetadata, Map<String, Object> input) {
    Item item = new Item();
    // TODO
    return item;
  }

  @Override
  protected Entry createAtomEntry(FeedMetadata feedMetadata, Map<String, Object> input) {
    Entry entry = new Entry();
    // TODO
    return entry;
  }

}
