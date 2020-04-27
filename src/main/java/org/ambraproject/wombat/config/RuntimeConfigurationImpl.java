/*
 * Copyright (c) 2020 Public Library of Science
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

package org.ambraproject.wombat.config;

import java.net.URI;
import java.net.URL;
import com.google.common.base.Preconditions;


public class RuntimeConfigurationImpl implements RuntimeConfiguration {
  String memcachedServer;
  URL rhinoUrl;
  String rootRedirect;
  String environment;
  String themePath;
  String casUrl;
  URL solrUrl;
  URI nedUrl;

  @Override
  public URI getNedUrl() {
    return nedUrl;
  }

  public void setNedUrl(URI nedUrl) {
    Preconditions.checkNotNull(nedUrl, "Please set NED_URL.");
    Preconditions.checkArgument(!nedUrl.toString().equals(""), "Please set NED_URL.");
    Preconditions.checkArgument(((nedUrl.getUserInfo() != null) &&
                                 !nedUrl.getUserInfo().equals("") &&
                                 nedUrl.getUserInfo().split(":").length == 2),
        "Please set username, password in NED_URL");
    this.nedUrl = nedUrl;
  }

  @Override
  public URL getSolrUrl() {
    return solrUrl;
  }

  public void setSolrUrl(URL solrUrl) {
    Preconditions.checkNotNull(solrUrl, "Please set SOLR_URL.");
    Preconditions.checkArgument(!solrUrl.toString().equals(""), "Please set SOLR_URL.");
    this.solrUrl = solrUrl;
  }

  @Override
  public String getCasUrl() {
    return casUrl;
  }

  public void setCasUrl(String casUrl) {
    Preconditions.checkNotNull(casUrl, "Please set CAS_URL.");
    Preconditions.checkArgument(!casUrl.equals(""), "Please set CAS_URL.");
    this.casUrl = casUrl;
  }

  @Override
  public String getThemePath() {
    return themePath;
  }

  public void setThemePath(String themePath) {
    Preconditions.checkNotNull(themePath, "Please set THEME_PATH.");
    Preconditions.checkArgument(!themePath.equals(""), "Please set THEME_PATH.");
    this.themePath = themePath;
  }

  @Override
  public String getEnvironment() {
    return environment;
  }

  public void setEnvironment(String environment) {
    Preconditions.checkNotNull(environment, "Please set ENVIRONMENT.");
    Preconditions.checkArgument(!environment.equals(""), "Please set ENVIRONMENT.");
    this.environment = environment;
  }

  @Override
  public String getRootRedirect() {
    return rootRedirect;
  }

  public void setRootRedirect(String rootRedirect) {
    Preconditions.checkNotNull(rootRedirect, "Please set ROOT_REDIRECT.");
    Preconditions.checkArgument(!rootRedirect.equals(""), "Please set ROOT_REDIRECT.");
    this.rootRedirect = rootRedirect;
  }

  @Override
  public URL getRhinoUrl() {
    return rhinoUrl;
  }

  public void setRhinoUrl(URL rhinoUrl) {
    Preconditions.checkNotNull(rhinoUrl, "Please set RHINO_URL.");
    Preconditions.checkArgument(!rhinoUrl.toString().equals(""), "Please set RHINO_URL.");
    this.rhinoUrl = rhinoUrl;
  }

  @Override
  public String getMemcachedServer() {
    return memcachedServer;
  }

  public void setMemcachedServer(String memcachedServer) {
    this.memcachedServer = memcachedServer;
  }
}

