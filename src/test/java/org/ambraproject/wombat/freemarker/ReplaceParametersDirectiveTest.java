package org.ambraproject.wombat.freemarker;


import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import freemarker.template.SimpleHash;
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
    Map directiveParams = new HashMap();
    directiveParams.put("name", "paramToReplace");
    directiveParams.put("value", "newValue");

    Multimap<String, String> actual = ReplaceParametersDirective.replaceParameters(new SimpleHash(parameters),
        directiveParams);
    ImmutableSetMultimap.Builder<String, String> expected = ImmutableSetMultimap.builder();
    expected.put("foo", "fooValue")
        .putAll("multiValuedParam", "value1", "value2")
        .put("emptyParam", "")
        .put("paramToReplace", "newValue");
    assertEquals(actual, expected.build());

  }
}
