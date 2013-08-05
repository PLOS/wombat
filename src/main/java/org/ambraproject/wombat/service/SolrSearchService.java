/*
 * $HeadURL$
 * $Id$
 * Copyright (c) 2006-2013 by Public Library of Science http://plos.org http://ambraproject.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ambraproject.wombat.service;

import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of SearchService that queries a solr backend.
 */
public class SolrSearchService extends JsonService implements SearchService {

  /**
   * Specifies the article fields in the solr schema that we want returned in the results.
   */
  private static final String FL = "id,publication_date,title,cross_published_journal_name,author_display,article_type";

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<?, ?> simpleSearch(String query, String journal, int start, int rows) throws IOException {

    // Fascinating how painful it is to construct a longish URL and escape it properly in Java.
    // This is the easiest way I found...
    List<NameValuePair> params = new ArrayList<NameValuePair>();

    // TODO: escape/quote the q param if needed.
    params.add(new BasicNameValuePair("q", "everything:" + query));
    params.add(new BasicNameValuePair("wt", "json"));
    params.add(new BasicNameValuePair("fl", FL));
    params.add(new BasicNameValuePair("fq", "doc_type:full"));
    params.add(new BasicNameValuePair("fq", "!article_type_facet:\"Issue Image\""));
    params.add(new BasicNameValuePair("rows", Integer.toString(rows)));
    if (start > 1) {
      params.add(new BasicNameValuePair("start", Integer.toString(start)));
    }
    // The next two params improve solr performance significantly.
    params.add(new BasicNameValuePair("hl", "false"));
    params.add(new BasicNameValuePair("facet", "false"));

    // TODO: sort order.  This is the ambra default.
    params.add(new BasicNameValuePair("sort", "score desc,publication_date desc,id desc"));
    URI uri;
    try {
      uri = new URL(runtimeConfiguration.getSolrServer(), "select?" + URLEncodedUtils.format(params, "UTF-8")).toURI();
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(e);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
    Map<?, ?> rawResults = requestObject(uri, Map.class);
    Map responseHeader = (Map) rawResults.get("responseHeader");

    // I know it's bad form to do equality checking on floating-point numbers, but
    // JSON only has one type for number and this should be safe...
    if (!responseHeader.get("status").equals(new Double(0.0))) {
      throw new RuntimeException("Solr server returned status " + responseHeader.get("status"));
    } else {
      return (Map<?, ?>)rawResults.get("response");
    }
  }
}
