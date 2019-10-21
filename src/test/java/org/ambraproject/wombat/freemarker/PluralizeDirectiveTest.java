package org.ambraproject.wombat.freemarker;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.ambraproject.wombat.freemarker.PluralizeDirective.pluralize;

public class PluralizeDirectiveTest {

  @Test
  public void testPluralize() {
    assertEquals("asserts", pluralize("assert"));
    assertEquals("Asserts", pluralize("Assert"));
    assertEquals("ASSERTS", pluralize("ASSERT"));
    assertEquals("commentaries", pluralize("commentary"));
    assertEquals("COMMENTARIES", pluralize("COMMENTARY"));
    assertEquals("Expressions of Concern", pluralize("Expression of Concern"));
  }
}
