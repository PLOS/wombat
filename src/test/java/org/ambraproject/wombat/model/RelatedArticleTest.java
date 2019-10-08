package org.ambraproject.wombat.model;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class RelatedArticleTest {
  @Test
  public void testCreateFromMap() {
    Map<String, String> map = new HashMap<String, String>();
    map.put("doi", "10.1371/journal.pxxx.000000");
    map.put("title", "A new article");
    map.put("publicationDate", "2019-10-08");
    RelatedArticle ra = RelatedArticle.fromMap(map);
    assertEquals(map.get("doi"), ra.getDoi());
    assertEquals(map.get("title"), ra.getTitle());
    assertEquals(LocalDate.of(2019, 10, 8), ra.getPublicationDate());
  }
}
