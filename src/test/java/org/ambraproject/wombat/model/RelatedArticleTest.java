package org.ambraproject.wombat.model;

import static org.ambraproject.wombat.util.FileUtils.read;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.google.gson.Gson;

import org.ambraproject.wombat.controller.ArticleMetadata;
import org.junit.Test;

public class RelatedArticleTest {
  String doi = "10.1371/journal.ppat.1006021";
  String title = "<article-title xmlns:mml=\"http://www.w3.org/1998/Math/MathML\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">Correction: Influenza Virus Targets Class I MHC-Educated NK Cells for Immunoevasion</article-title>";
  RelatedArticleJournal journal = RelatedArticleJournal.builder()
    .seteIssn("1553-7374")
    .setJournalKey("PLoSPathogens")
    .setTitle("PLOS Pathogens")
    .build();

  @Test
  public void testCreateFromJson() throws IOException {
    RelatedArticle ra = new Gson().<List<RelatedArticle>>fromJson(read("articleMeta/ppat.1005446.related.json"),
                                                                  ArticleMetadata.Factory.RELATED_ARTICLE_GSON_TYPE).get(0);

    assertEquals(doi, ra.getDoi());
    assertEquals(title, ra.getTitle());
    assertEquals(new Integer(1), ra.getRevisionNumber());
    assertEquals(LocalDate.of(2016, 11, 4), ra.getPublicationDate());
    assertEquals(RelatedArticleType.get("correction-forward"), ra.getType());
    assertEquals(journal, ra.getJournal());
    assertTrue(ra.isPublished());
  }
}
