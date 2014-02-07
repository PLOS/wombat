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

import freemarker.core.Environment;
import freemarker.ext.servlet.HttpRequestParametersHashModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelIterator;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Freemarker custom directive that writes out URL parameters based on the current request parameters.  A single
 * parameter's value can be replaced or added.
 * <p/>
 * There is one required parameter named params.  This should be an instance of {@link HttpRequestParametersHashModel},
 * which is available in freemarker templates as RequestParameters.
 * <p/>
 * Optional parameters are name and value.  If present, the named parameter will be added (or replaced if it is already
 * present) with the value.
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
    Map<String, String> outputParams = new HashMap<>();

    // I have no idea why freemarker feels the need to invent their own collection classes...
    HttpRequestParametersHashModel requestParams = (HttpRequestParametersHashModel) params.get("params");
    TemplateModelIterator iter = requestParams.keys().iterator();
    while (iter.hasNext()) {
      String key = iter.next().toString();
      TemplateModel value = requestParams.get(key.toString());
      outputParams.put(key, value.toString());
    }
    Object name = params.get("name");
    if (name != null) {
      outputParams.put(name.toString(), params.get("value").toString());
    }
    List<NameValuePair> paramList = new ArrayList<>(outputParams.size());
    for (Object key : outputParams.keySet()) {
      paramList.add(new BasicNameValuePair((String) key, (String) outputParams.get(key)));
    }
    environment.getOut().write(URLEncodedUtils.format(paramList, "UTF-8"));
  }
}
