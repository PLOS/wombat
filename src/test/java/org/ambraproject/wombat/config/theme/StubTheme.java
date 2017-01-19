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

package org.ambraproject.wombat.config.theme;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import freemarker.cache.TemplateLoader;
import org.apache.commons.io.input.ReaderInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class StubTheme extends Theme {

  private final String journalKey;

  public StubTheme(String key, String journalKey, Theme... parents) {
    super(key, ImmutableList.copyOf(parents));
    this.journalKey = Preconditions.checkNotNull(journalKey);
  }

  @Override
  public TemplateLoader getTemplateLoader() throws IOException {
    return null;
  }

  @Override
  protected InputStream fetchStaticResource(String path) throws IOException {
    if (path.equals("config/journal.json")) {
      Map<String, Object> journalConfigMap = getJournalConfigMap();
      return new ReaderInputStream(new StringReader(new Gson().toJson(journalConfigMap)));
    }
    return null;
  }

  protected Map<String, Object> getJournalConfigMap() {
    Map<String, Object> configMap = new LinkedHashMap<>();
    configMap.put("journalKey", journalKey);
    configMap.put("journalName", journalKey);
    return configMap;
  }

  @Override
  protected ResourceAttributes fetchResourceAttributes(String path) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  protected Collection<String> fetchStaticResourcePaths(String root) throws IOException {
    throw new UnsupportedOperationException();
  }

}
