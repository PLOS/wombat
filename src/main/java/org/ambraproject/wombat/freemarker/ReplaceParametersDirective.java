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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import freemarker.core.Environment;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Freemarker custom directive that writes out URL parameters based on the current request parameters. Parameter values
 * can be replaced or added.
 * <p>
 * A required directive parameter named "parameterMap" should be a map of the request parameters.
 * <p>
 * The other directive parameter is "replacements", which is a FreeMarker hash of URL parameter names and values to
 * replace. All URL parameters given this way will be added, or all values will be replaced if it is already present.
 * The value may be a sequence, in which case multiple URL parameters with the same name will be added (replacing any
 * number of parameters with that name).
 * <p>
 * Example usage:
 * <p>
 * <a href="foo?<@replaceParams parameterMap=RequestParameters replacements={"bar": "baz"} />">link</a>
 * <p>
 * This will write out a URL beginning with foo and including all the parameters in the current request, with an
 * additional parameter "bar" added (or replaced) with the value "baz".
 */
public class ReplaceParametersDirective implements TemplateDirectiveModel {

  private static final String PARAMETER_MAP_KEY = "parameterMap";
  private static final String REPLACEMENTS_KEY = "replacements";
  private static final ImmutableSet<String> EXPECTED_KEYS = ImmutableSet.of(PARAMETER_MAP_KEY, REPLACEMENTS_KEY);

  public void execute(Environment environment, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
      throws TemplateException, IOException {
    if (!params.keySet().equals(EXPECTED_KEYS)) {
      throw new TemplateException("ReplaceParametersDirective requires keys: " + EXPECTED_KEYS, environment);
    }

    // I have no idea why freemarker feels the need to invent their own collection classes...
    SimpleHash parameterMap = (SimpleHash) params.get(PARAMETER_MAP_KEY);
    Multimap<String, TemplateModel> replacements = TemplateModelUtil.getAsMultimap((TemplateModel) params.get(REPLACEMENTS_KEY));
    Multimap<String, String> outputParams = replaceParameters(parameterMap, replacements);
    List<NameValuePair> paramList = new ArrayList<>(outputParams.size());
    for (String key : outputParams.keySet()) {
      for (String value : outputParams.get(key)) {
        paramList.add(new BasicNameValuePair(key, value));
      }
    }
    environment.getOut().write(URLEncodedUtils.format(paramList, "UTF-8"));
  }

  @VisibleForTesting
  static Multimap<String, String> replaceParameters(SimpleHash parameterMap,
                                                    Multimap<String, TemplateModel> replacements)
      throws TemplateException {
    Multimap<String, String> result = HashMultimap.create();

    // The map is passed in as a Map<String, String[]>, but Freemarker doesn't support generics
    // (and wraps the map in its own data structure).
    Map map = parameterMap.toMap();
    Iterator iter = map.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry entry = (Map.Entry) iter.next();
      String[] values = (String[]) entry.getValue();
      for (String value : values) {
        result.put((String) entry.getKey(), value);
      }
    }

    for (Map.Entry<String, Collection<TemplateModel>> replacementEntry : replacements.asMap().entrySet()) {
      Collection<String> replacementValues = Collections2.transform(replacementEntry.getValue(), Object::toString);
      result.replaceValues(replacementEntry.getKey(), replacementValues);
    }

    return ImmutableSetMultimap.copyOf(result);
  }
}
