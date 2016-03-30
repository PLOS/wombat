package org.ambraproject.wombat.service.remote;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

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
