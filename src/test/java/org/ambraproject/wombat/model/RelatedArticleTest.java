package org.ambraproject.wombat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Test;

public class RelatedArticleTest {
  @Test
  public void testCreateFromMap() {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("doi", "10.1371/journal.pxxx.000000");
    map.put("title", "A new article");
    map.put("revisionNumber", new Double(1));
    map.put("publicationDate", "2019-10-08");
    RelatedArticle ra = RelatedArticle.fromMap(map);
    assertEquals(map.get("doi"), ra.getDoi());
    assertEquals(map.get("title"), ra.getTitle());
    assertEquals(Optional.of(1), ra.getRevisionNumber());
    assertEquals(LocalDate.of(2019, 10, 8), ra.getPublicationDate());
    assertTrue(ra.isPublished());
  }

  @Test
  public void testCreateFromMapNullRevisionNumber() {
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("doi", "10.1371/journal.pxxx.000000");
    map.put("title", "A new article");
    map.put("publicationDate", "2019-10-08");
    RelatedArticle ra = RelatedArticle.fromMap(map);
    assertEquals(map.get("doi"), ra.getDoi());
    assertEquals(map.get("title"), ra.getTitle());
    assertEquals(Optional.empty(), ra.getRevisionNumber());
    assertEquals(LocalDate.of(2019, 10, 8), ra.getPublicationDate());
    assertFalse(ra.isPublished());
  }
}
