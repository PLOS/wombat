package org.ambraproject.wombat.rss;

import com.google.common.base.Strings;
import com.rometools.rome.feed.rss.Channel;
import com.rometools.rome.feed.rss.Content;
import com.rometools.rome.feed.rss.Item;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.url.Link;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.view.feed.AbstractRssFeedView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
      item.setContent(buildContent(article));
      // TODO: Fill more values
      return item;
    }

    private Content buildContent(Map<String, ?> article) {
      Content content = new Content();
      // TODO: Fill values
      return content;
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
