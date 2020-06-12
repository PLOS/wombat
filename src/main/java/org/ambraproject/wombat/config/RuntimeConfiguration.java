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

package org.ambraproject.wombat.config;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.UtcDateTypeAdapter;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import org.ambraproject.wombat.util.JodaTimeLocalDateAdapter;

/**
 * Class that represents configurable values that are only known at server
 * startup time.
 */
public class RuntimeConfiguration {
  String memcachedServer;
  URL rhinoUrl;
  URL collectionsUrl;
  String rootRedirect;
  boolean debug;
  String themePath;
  String casUrl;
  URL solrUrl;
  URI nedUrl;
  String bugsnagApiKey;
  String bugsnagReleaseStage;
  
  public static Gson makeGson() {
    GsonBuilder builder = new GsonBuilder();
    builder.setPrettyPrinting();
    builder.registerTypeAdapter(Date.class, new UtcDateTypeAdapter());
    builder.registerTypeAdapter(org.joda.time.LocalDate.class,
                                JodaTimeLocalDateAdapter.INSTANCE);
    return builder.create();
  }

  public URI getNedUrl() { return nedUrl; }

  public void setNedUrl(URI nedUrl) {
    Preconditions.checkNotNull(nedUrl, "Please set NED_URL.");
    Preconditions.checkArgument(!nedUrl.toString().equals(""),
                                "Please set NED_URL.");
    Preconditions.checkArgument(((nedUrl.getUserInfo() != null) &&
                                 !nedUrl.getUserInfo().equals("") &&
                                 nedUrl.getUserInfo().split(":").length == 2),
                                "Please set username, password in NED_URL");
    this.nedUrl = nedUrl;
  }

  public URL getSolrUrl() { return solrUrl; }

  public void setSolrUrl(URL solrUrl) {
    Preconditions.checkNotNull(solrUrl, "Please set SOLR_URL.");
    Preconditions.checkArgument(!solrUrl.toString().equals(""),
                                "Please set SOLR_URL.");
    this.solrUrl = solrUrl;
  }

  public String getCasUrl() { return casUrl; }

  public void setCasUrl(String casUrl) {
    Preconditions.checkNotNull(casUrl, "Please set CAS_URL.");
    Preconditions.checkArgument(!casUrl.equals(""), "Please set CAS_URL.");
    this.casUrl = casUrl;
  }

  public String getThemePath() { return themePath; }

  public void setThemePath(String themePath) {
    Preconditions.checkNotNull(themePath, "Please set THEME_PATH.");
    Preconditions.checkArgument(!themePath.equals(""),
                                "Please set THEME_PATH.");
    this.themePath = themePath;
  }

  /**
   * Show debugging information?
   */
  public boolean showDebug() { return this.debug; }

  public void setDebug(boolean debug) { this.debug = debug; }

  /**
   * Get the path of an HTML document to display on the root page
   */
  public String getRootRedirect() { return rootRedirect; }

  public void setRootRedirect(String rootRedirect) {
    Preconditions.checkNotNull(rootRedirect, "Please set ROOT_REDIRECT.");
    Preconditions.checkArgument(!rootRedirect.equals(""),
                                "Please set ROOT_REDIRECT.");
    this.rootRedirect = rootRedirect;
  }

  /**
   * Get the URL of the rhino server.
   *
   * @return the URL
   */

  public URL getRhinoUrl() { return rhinoUrl; }

  public void setRhinoUrl(URL rhinoUrl) {
    Preconditions.checkNotNull(rhinoUrl, "Please set RHINO_URL.");
    Preconditions.checkArgument(!rhinoUrl.toString().equals(""),
                                "Please set RHINO_URL.");
    this.rhinoUrl = rhinoUrl;
  }

  /**
   * @return the memcached host, or null if it is not present in the config
   */
  public String getMemcachedServer() { return memcachedServer; }

  public void setMemcachedServer(String memcachedServer) {
    this.memcachedServer = memcachedServer;
  }

  public URL getCollectionsUrl() { return collectionsUrl; }

  public void setCollectionsUrl(URL collectionsUrl) {
    Preconditions.checkNotNull(collectionsUrl, "Please set COLLECTIONS_URL.");
    Preconditions.checkArgument(!collectionsUrl.toString().equals(""),
                                "Please set COLLECTIONS_URL.");
    this.collectionsUrl = collectionsUrl;
  }

  public String getBugsnagApiKey() {
    return this.bugsnagApiKey;
  }

  public void setBugsnagApiKey(String bugsnagApiKey) {
    this.bugsnagApiKey = bugsnagApiKey;
  }

  public String getBugsnagReleaseStage() {
    return this.bugsnagReleaseStage;
  }

  public void setBugsnagReleaseStage(String bugsnagReleaseStage) {
    this.bugsnagReleaseStage = bugsnagReleaseStage;
  }
}
