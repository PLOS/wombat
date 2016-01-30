package org.ambraproject.wombat.freemarker;


import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModel;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class ReplaceParametersDirectiveTest {

  @Test
  public void testReplaceParameters() throws Exception {
    Map<String, String[]> parameters = new HashMap<>();
    parameters.put("foo", new String[]{"fooValue"});
    parameters.put("multiValuedParam", new String[]{"value1", "value2"});
    parameters.put("emptyParam", new String[]{""});
    parameters.put("paramToReplace", new String[]{"oldValue"});
    Multimap<String, TemplateModel> replacements = ImmutableMultimap.of("paramToReplace", new SimpleScalar("newValue"));

    Multimap<String, String> actual = ReplaceParametersDirective.replaceParameters(new SimpleHash(parameters),
        replacements);
    ImmutableSetMultimap.Builder<String, String> expected = ImmutableSetMultimap.builder();
    expected.put("foo", "fooValue")
        .putAll("multiValuedParam", "value1", "value2")
        .put("emptyParam", "")
        .put("paramToReplace", "newValue");
    assertEquals(actual, expected.build());

  }
}
