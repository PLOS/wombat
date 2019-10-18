package org.ambraproject.wombat.model;

import static org.ambraproject.wombat.util.FileUtils.read;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.Test;

public class RelatedArticleTest {
  String doi = "10.1371/journal.ppat.1006021";
  Optional<String> title = Optional.of("<article-title xmlns:mml=\"http://www.w3.org/1998/Math/MathML\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">Correction: Influenza Virus Targets Class I MHC-Educated NK Cells for Immunoevasion</article-title>");

  @Test
  public void testCreateFromJson() {
    Type t = TypeToken.getParameterized(List.class, RelatedArticle.class).getType();
    RelatedArticle ra = new Gson().<List<RelatedArticle>>fromJson(read("articleMeta/ppat.1005446.related.json"), t).get(0);

    assertEquals(doi, ra.getDoi());
    assertEquals(title, ra.getTitle());
    assertEquals(Optional.of(1), ra.getRevisionNumber());
    assertEquals(Optional.of(LocalDate.of(2016, 11, 4)), ra.getPublicationDate());
    assertEquals("correction-forward", ra.getType());
    assertTrue(ra.isPublished());
  }
}
