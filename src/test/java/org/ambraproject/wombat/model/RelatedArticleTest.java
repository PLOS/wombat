package org.ambraproject.wombat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

public class RelatedArticleTest {
  String doi = "10.1371/journal.pxxx.000000";
  String title = "A new article";
  String publicationDate = "2019-10-08";
  Map<String, Object> map = new HashMap<String, Object>();
  String type = "retracted-article";

  RelatedArticle.ArticleMetadata otherArticle = RelatedArticle.ArticleMetadata.builder()
    .setDoi("10.9999/journal.pxxx.1")
    .setTitle(Optional.of("Other article"))
    .setRevisionNumber(Optional.of(2))
    .setPublicationDate(Optional.of(LocalDate.of(2009, 1, 1)))
    .build();

  @Before
  public void setup() {
    map.put("doi", doi);
    map.put("title", title);
    map.put("revisionNumber", new Double(1));
    map.put("publicationDate", publicationDate);
    map.put("type", type);
  }

  @Test
  public void testRoundTripTypeInverstion() {
    for (String type: RelatedArticle.invertedTypes.keySet()) {
      String inverted = RelatedArticle.invertedTypes.get(type);
      String doubleInverted = RelatedArticle.invertedTypes.get(inverted);
      assertEquals(type, doubleInverted);
    }
  }

  @Test
  public void testCreateFromInboundMap() {
    RelatedArticle ra = RelatedArticle.fromInboundMap(otherArticle, map);
    assertEquals(otherArticle, ra.getTarget());
    assertEquals(doi, ra.getSource().getDoi());
    assertEquals(Optional.of(title), ra.getSource().getTitle());
    assertEquals(Optional.of(1), ra.getSource().getRevisionNumber());
    assertEquals(Optional.of(LocalDate.of(2019, 10, 8)), ra.getSource().getPublicationDate());
    assertEquals(map.get("type"), ra.getType());
    assertTrue(ra.getSource().isPublished());
  }

  @Test
  public void testCreateFromOutboundMapNullRevisionNumber() {
    map.remove("revisionNumber");
    RelatedArticle ra = RelatedArticle.fromOutboundMap(otherArticle, map);
    assertEquals(otherArticle, ra.getSource());
    assertEquals(doi, ra.getTarget().getDoi());
    assertEquals(Optional.of(title), ra.getTarget().getTitle());
    assertEquals(Optional.of(LocalDate.of(2009, 1, 1)), ra.getSource().getPublicationDate());
    assertEquals(map.get("type"), ra.getType());
    assertFalse(ra.getTarget().isPublished());
  }

  @Test
  public void testInvert() {
    RelatedArticle ra = RelatedArticle.fromOutboundMap(otherArticle, map);
    RelatedArticle inverted = ra.invert();
    assertEquals(ra.getSource(), inverted.getTarget());
    assertEquals(ra.getTarget(), inverted.getSource());
    assertEquals("retraction-forward", inverted.getType());
  }
}
