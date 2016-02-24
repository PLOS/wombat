package org.ambraproject.wombat.feed;

import com.google.common.collect.ImmutableList;
import com.rometools.rome.feed.atom.Category;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.feed.rss.Guid;
import com.rometools.rome.feed.rss.Item;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.service.CommentFormatting;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class CommentFeedView extends AbstractFeedView<Map<String, Object>> {

  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;

  private String getCommentUrl(FeedMetadata feedMetadata, String commentId) {
    return feedMetadata.buildLink(link -> link
        .toPattern(requestMappingContextDictionary, "articleComments")
        .addQueryParameter("id", commentId)
        .build());
  }

  private String getArticleUrl(FeedMetadata feedMetadata, Map<String, Object> article) {
    return feedMetadata.buildLink(link -> link
        .toPattern(requestMappingContextDictionary, "article")
        .addQueryParameter("id", article.get("doi"))
        .build());
  }

  private static String getCreatorDisplayName(Map<String, Object> comment) {
    Map<String, Object> creator = (Map<String, Object>) comment.get("creator");
    return (String) creator.get("displayName");
  }

  private static Date getCommentDate(Map<String, Object> comment) {
    return Date.from(Instant.parse((String) comment.get("lastModified")));
  }

  private String createCommentHtml(FeedMetadata feedMetadata, Map<String, Object> comment) {
    Map<String, Object> article = (Map<String, Object>) comment.get("parentArticle");
    String articleTitle = (String) Objects.requireNonNull(article.get("title"));

    String header = String.format("<p>Comment on <a href=\"%s\">%s</a></p>\n",
        getArticleUrl(feedMetadata, article), articleTitle);

    CommentFormatting.FormattedComment formatted = new CommentFormatting.FormattedComment(comment);
    return header + formatted.getBodyWithHighlightedText();
  }

  @Override
  protected Item createRssItem(FeedMetadata feedMetadata, Map<String, Object> comment) {
    String commentId = (String) comment.get("annotationUri");

    Item item = new Item();
    item.setTitle((String) comment.get("title"));
    item.setLink(getCommentUrl(feedMetadata, commentId));
    item.setAuthor(getCreatorDisplayName(comment));
    item.setPubDate(getCommentDate(comment));

    Guid guid = new Guid();
    guid.setValue(commentId);
    guid.setPermaLink(false);
    item.setGuid(guid);

    com.rometools.rome.feed.rss.Content content = new com.rometools.rome.feed.rss.Content();
    content.setType(com.rometools.rome.feed.rss.Content.HTML);
    content.setValue(createCommentHtml(feedMetadata, comment));
    item.setContent(content);

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
    entry.setAuthors(ImmutableList.of(createAtomPerson(getCreatorDisplayName(comment))));
    createAtomCategory(comment).map(ImmutableList::of).ifPresent(entry::setCategories);

    Link commentLink = createAtomLink(getCommentUrl(feedMetadata, commentId),
        commentTitle, Optional.empty(), Optional.empty());
    Link articleLink = createAtomLink(getArticleUrl(feedMetadata, article),
        (String) article.get("title"), Optional.of("related"), Optional.empty());
    entry.setAlternateLinks(ImmutableList.of(commentLink, articleLink));

    Date lastModified = getCommentDate(comment);
    entry.setPublished(lastModified);
    entry.setUpdated(lastModified);

    com.rometools.rome.feed.atom.Content content = new com.rometools.rome.feed.atom.Content();
    content.setType(com.rometools.rome.feed.atom.Content.HTML);
    content.setValue(createCommentHtml(feedMetadata, comment));
    entry.setContents(ImmutableList.of(content));

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
