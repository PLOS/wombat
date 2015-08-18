/*
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.wombat.freemarker;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import freemarker.core.Environment;
import freemarker.ext.servlet.HttpRequestParametersHashModel;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Freemarker custom directive that writes out URL parameters based on the current request parameters.  A single
 * parameter's values can be replaced or added.
 * <p/>
 * There is one required parameter named params.  This should be a map of the request parameters.
 * <p/>
 * Optional parameters are name and value.  If present, the named parameter will be added (or all values
 * will be replaced if it is already present).
 * <p/>
 * Example usage:
 * <p/>
 * <a href="foo?<@replaceParams params=RequestParameters name="bar" value="baz" />">link</a>
 * <p/>
 * This will write out a URL beginning with foo and including all the parameters in the current request, with an
 * additional parameter "bar" added (or replaced) with the value "baz".
 * <p/>
 * TODO: add the ability to add/replace multiple params, if that is needed.
 */
public class ReplaceParametersDirective implements TemplateDirectiveModel {

  public void execute(Environment environment, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
      throws TemplateException, IOException {

    // I have no idea why freemarker feels the need to invent their own collection classes...
    SimpleHash parameterMap = (SimpleHash) params.get("parameterMap");
    Multimap<String, String> outputParams = replaceParameters(parameterMap, params);
    List<NameValuePair> paramList = new ArrayList<>(outputParams.size());
    for (String key : outputParams.keySet()) {
      for (String value : outputParams.get(key)) {
        paramList.add(new BasicNameValuePair(key, value));
      }
    }
    environment.getOut().write(URLEncodedUtils.format(paramList, "UTF-8"));
  }

  @VisibleForTesting
  static Multimap<String, String> replaceParameters(SimpleHash parameterMap, Map directiveParams)
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
    Object name = directiveParams.get("name");
    if (name != null) {
      result.removeAll(name.toString());
      result.put(name.toString(), directiveParams.get("value").toString());
    }
    return ImmutableSetMultimap.copyOf(result);
  }
}
