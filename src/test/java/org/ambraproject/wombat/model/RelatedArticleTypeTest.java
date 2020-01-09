package org.ambraproject.wombat.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import java.util.List;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class RelatedArticleTypeTest {
  @Test
  public void testGet() {
    assertNotEquals(RelatedArticleType.get("corrected-article"),
        RelatedArticleType.get("correction-forward"));
    assertEquals(RelatedArticleType.get("corrected-article"),
        RelatedArticleType.get("corrected-article"));
    assertEquals("Correction",
                 RelatedArticleType.get("corrected-article").getDisplayName());
  }

  @Test
  public void testGetSpecificUse() {
    RelatedArticleType type = RelatedArticleType.get("updated-article", "registered-report-protocol");
    assertNotEquals(RelatedArticleType.get("updated-article"), type);
    assertEquals("registered-report-protocol", type.getSpecificUse());
  }

  @Test
  public void testGetUnknown() {
    RelatedArticleType type = RelatedArticleType.get("foobar");
    assertNotNull(type);
    assertEquals(type.getName(), "foobar");
    assertEquals(type.getDisplayName(), "foobar");
  }

  @Test
  public void testSort() {
    List<RelatedArticleType> unordered = ImmutableList
      .of(RelatedArticleType.get("companion"),
          RelatedArticleType.get("foobar"),
          RelatedArticleType.get("retracted-article"));
    List<RelatedArticleType> ordered = ImmutableList
      .of(RelatedArticleType.get("retracted-article"),
          RelatedArticleType.get("companion"),
          RelatedArticleType.get("foobar"));
    List<RelatedArticleType> reordered = ImmutableList.sortedCopyOf(unordered);
    assertEquals(ordered, reordered);
  }
}
