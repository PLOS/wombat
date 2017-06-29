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

import freemarker.core.Environment;
import freemarker.template.TemplateException;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * Accesses global config values from the {@link RuntimeConfiguration} bean.
 * <p>
 * Not all {@link RuntimeConfiguration} getter methods are supported. They must be supported individually by defining
 * key strings, according to needs in template code as they are discovered.
 */
public class GlobalConfigDirective extends VariableLookupDirective<Object> {

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  @Override
  protected Object getValue(Environment env, Map params) throws TemplateException, IOException {
    Object key = params.get("key");
    if (key == null) {
      throw new TemplateException("key string required", env);
    }

    switch (key.toString()) {
      case "solrServer":
        if (!runtimeConfiguration.getSolrConfiguration().isPresent()) { return null; }
        return runtimeConfiguration.getSolrConfiguration().get().getUrl().map(URL::toString).orElse(null);
      case "isCasAvailable":
        return runtimeConfiguration.getCasConfiguration().isPresent();
      // More cases may be added to expose other runtimeConfiguration getters as needed
      default:
        throw new TemplateException("key not matched", env);
    }
  }

}
