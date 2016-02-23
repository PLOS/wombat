package org.ambraproject.wombat.feed;

import com.google.common.collect.ImmutableList;
import com.rometools.rome.feed.atom.Category;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.rss.Item;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

public class CommentFeedView extends AbstractFeedView<Map<String, Object>> {

  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;

  @Override
  protected Item createRssItem(FeedMetadata feedMetadata, Map<String, Object> comment) {
    Item item = new Item();
    // TODO
    return item;
  }

  @Override
  protected Entry createAtomEntry(FeedMetadata feedMetadata, Map<String, Object> comment) {
    Map<String, Object> article = (Map<String, Object>) comment.get("parentArticle");
    String commentId = (String) comment.get("annotationUri");
    String commentTitle = (String) comment.get("title");

    Entry entry = new Entry();
    entry.setId(commentId);
    entry.setTitle(commentTitle);
    createAtomCategory(comment).map(ImmutableList::of).ifPresent(entry::setCategories);

    Link commentLink = createAtomLink(feedMetadata.buildLink(link -> link
            .toPattern(requestMappingContextDictionary, "articleComments")
            .addQueryParameter("id", commentId)
            .build()),
        commentTitle, Optional.empty(), Optional.empty());
    Link articleLink = createAtomLink(feedMetadata.buildLink(link -> link
            .toPattern(requestMappingContextDictionary, "article")
            .addQueryParameter("id", article.get("doi"))
            .build()),
        (String) article.get("title"), Optional.of("related"), Optional.empty());
    entry.setAlternateLinks(ImmutableList.of(commentLink, articleLink));

    Map<String, Object> creator = (Map<String, Object>) comment.get("creator");
    String displayName = (String) creator.get("displayName");
    entry.setAuthors(ImmutableList.of(createAtomPerson(displayName)));

    Date lastModified = Date.from(Instant.parse((String) comment.get("lastModified")));
    entry.setPublished(lastModified);
    entry.setUpdated(lastModified);

    // TODO: setContents

    return entry;
  }

  private static Optional<Category> createAtomCategory(Map<String, Object> comment) {
    String type = (String) comment.get("type");
    if (type == null) return Optional.empty();
    Category category = new Category();
    category.setTerm(type);
    category.setLabel(type);
    return Optional.of(category);
  }

}
