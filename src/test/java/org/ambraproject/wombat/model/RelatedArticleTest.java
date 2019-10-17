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

  @Before
  public void setup() {
    map.put("doi", doi);
    map.put("title", title);
    map.put("revisionNumber", new Double(1));
    map.put("publicationDate", publicationDate);
    map.put("type", type);
  }


  @Test
  public void testCreateFromMap() {
    RelatedArticle ra = RelatedArticle.fromMap(map);
    assertEquals(doi, ra.getDoi());
    assertEquals(Optional.of(title), ra.getTitle());
    assertEquals(Optional.of(1), ra.getRevisionNumber());
    assertEquals(Optional.of(LocalDate.of(2019, 10, 8)), ra.getPublicationDate());
    assertEquals(map.get("type"), ra.getType());
    assertTrue(ra.isPublished());
  }
}
