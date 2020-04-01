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

import static com.google.common.base.Strings.isNullOrEmpty;
import java.net.URI;
import java.net.URL;
import com.google.common.base.Preconditions;

public class RuntimeConfigurationImpl implements RuntimeConfiguration {
  String memcachedServer;
  URL rhinoUrl;
  String rootPagePath;
  String environment;
  String themePath;
  String casUrl;
  URL solrUrl;
  URI nedUrl;
  String awsRoleArn;
  String editorialBucket;

  public RuntimeConfigurationImpl() {
    Preconditions.checkArgument(!isNullOrEmpty(System.getenv("AWS_ACCESS_KEY_ID")),
        "Please set AWS_ACCESS_KEY_ID.");
    Preconditions.checkArgument(!isNullOrEmpty(System.getenv("AWS_SECRET_ACCESS_KEY")),
        "Please set AWS_SECRET_ACCESS_KEY.");
  }

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
    Preconditions.checkArgument(!solrUrl.toString().isEmpty(), "Please set SOLR_URL.");
    this.solrUrl = solrUrl;
  }

  @Override
  public String getCasUrl() {
    return casUrl;
  }

  public void setCasUrl(String casUrl) {
    Preconditions.checkArgument(!isNullOrEmpty(casUrl), "Please set CAS_URL.");
    this.casUrl = casUrl;
  }

  @Override
  public String getThemePath() {
    return themePath;
  }

  public void setThemePath(String themePath) {
    Preconditions.checkArgument(!isNullOrEmpty(themePath), "Please set THEME_PATH.");
    this.themePath = themePath;
  }

  @Override
  public String getEnvironment() {
    return environment;
  }

  public void setEnvironment(String environment) {
    Preconditions.checkArgument(!isNullOrEmpty(environment), "Please set ENVIRONMENT.");
    this.environment = environment;
  }

  @Override
  public String getRootPagePath() {
    return rootPagePath;
  }

  public void setRootPagePath(String rootPagePath) {
    Preconditions.checkArgument(!isNullOrEmpty(rootPagePath), "Please set ROOT_PAGE_PATH.");
    this.rootPagePath = rootPagePath;
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

  @Override
  public String getAwsRoleArn() {
    return awsRoleArn;
  }

  public void setAwsRoleArn(String awsRoleArn) {
    Preconditions.checkState(!isNullOrEmpty(awsRoleArn), "AWS_ROLE_ARN is required");
    this.awsRoleArn = awsRoleArn;
  }

  @Override
  public String getEditorialBucket() {
    return editorialBucket;
  }

  public void setEditorialBucket(String editorialBucket) {
    this.editorialBucket = editorialBucket;
  }
}

