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

package org.ambraproject.wombat.model;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

/**
 * Specification for a search to be done to populate an alert. A JSON string representing this class is the value of
 * {@link org.plos.ned_client.model.Alert#query}.
 * Please note that these query parameters are the ones required by {@link org.ambraproject.service.search.SolrSearchService}
 * which is used by PLOS Queue to search against Solr. They should be updated as soon as the logic in queue
 * has been updated to talk to Solr directly.
 */
public class AlertQuery {

  private int startPage = 0;
  private List<String> filterSubjects = ImmutableList.of();
  private String unformattedQuery = "";
  private String query = "*:*";
  private String eLocationId = "";
  private int pageSize = 0;
  private String resultView = "";
  private String volume = "";
  private String sortValue = "";
  private String sortKey = "";
  private String filterKeyword = "";
  private List<String> filterArticleTypes = ImmutableList.of();
  private List<String> filterSubjectsDisjunction = ImmutableList.of();
  private List<String> filterAuthors = ImmutableList.of();
  private List<String> filterJournals = ImmutableList.of();
  private String id = "";

  public AlertQuery() {
  }

  public int getStartPage() {
    return startPage;
  }

  public void setStartPage(int startPage) {
    this.startPage = startPage;
  }

  public List<String> getFilterSubjects() {
    return filterSubjects;
  }

  public void setFilterSubjects(List<String> filterSubjects) {
    this.filterSubjects = Objects.requireNonNull(filterSubjects);
  }

  public String getUnformattedQuery() {
    return unformattedQuery;
  }

  public void setUnformattedQuery(String unformattedQuery) {
    this.unformattedQuery = Objects.requireNonNull(unformattedQuery);
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = Objects.requireNonNull(query);
  }

  public String geteLocationId() {
    return eLocationId;
  }

  public void seteLocationId(String eLocationId) {
    this.eLocationId = Objects.requireNonNull(eLocationId);
  }

  public int getPageSize() {
    return pageSize;
  }

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  public String getResultView() {
    return resultView;
  }

  public void setResultView(String resultView) {
    this.resultView = Objects.requireNonNull(resultView);
  }

  public String getVolume() {
    return volume;
  }

  public void setVolume(String volume) {
    this.volume = Objects.requireNonNull(volume);
  }

  public String getSortValue() {
    return sortValue;
  }

  public void setSortValue(String sortValue) {
    this.sortValue = Objects.requireNonNull(sortValue);
  }

  public String getSortKey() {
    return sortKey;
  }

  public void setSortKey(String sortKey) {
    this.sortKey = Objects.requireNonNull(sortKey);
  }

  public String getFilterKeyword() {
    return filterKeyword;
  }

  public void setFilterKeyword(String filterKeyword) {
    this.filterKeyword = Objects.requireNonNull(filterKeyword);
  }

  public List<String> getFilterArticleTypes() {
    return filterArticleTypes;
  }

  public void setFilterArticleTypes(List<String> filterArticleTypes) {
    this.filterArticleTypes = Objects.requireNonNull(filterArticleTypes);
  }

  public List<String> getFilterSubjectsDisjunction() {
    return filterSubjectsDisjunction;
  }

  public void setFilterSubjectsDisjunction(List<String> filterSubjectsDisjunction) {
    this.filterSubjectsDisjunction = Objects.requireNonNull(filterSubjectsDisjunction);
  }

  public List<String> getFilterAuthors() {
    return filterAuthors;
  }

  public void setFilterAuthors(List<String> filterAuthors) {
    this.filterAuthors = Objects.requireNonNull(filterAuthors);
  }

  public List<String> getFilterJournals() {
    return filterJournals;
  }

  public void setFilterJournals(List<String> filterJournals) {
    this.filterJournals = Objects.requireNonNull(filterJournals);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = Objects.requireNonNull(id);
  }
}
