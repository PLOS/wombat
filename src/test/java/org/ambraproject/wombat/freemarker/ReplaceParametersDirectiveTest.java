/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

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
