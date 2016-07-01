/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2011 by Public Library of Science
 *     http://plos.org
 *     http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambraproject.wombat.model;

import java.util.List;

/**
 * @author Alex Kudlick 11/8/11
 */
public class CitedArticle {

  private String key;
  private Integer year;
  private String displayYear;
  private String month;
  private String day;
  private Integer volumeNumber;
  private String volume;
  private String issue;
  private String title;
  private String publisherLocation;
  private String publisherName;
  private String pages;
  private String eLocationID;
  private String journal;
  private String note;
  private List<String> collaborativeAuthors;
  private String url;
  private String doi;
  private String summary;
  private String citationType;

  private List<CitedArticlePerson> authors;
  private List<CitedArticlePerson> editors;


  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
  }

  public String getDisplayYear() {
    return displayYear;
  }

  public void setDisplayYear(String displayYear) {
    this.displayYear = displayYear;
  }

  public String getMonth() {
    return month;
  }

  public void setMonth(String month) {
    this.month = month;
  }

  public String getDay() {
    return day;
  }

  public void setDay(String day) {
    this.day = day;
  }

  public Integer getVolumeNumber() {
    return volumeNumber;
  }

  public void setVolumeNumber(Integer volumeNumber) {
    this.volumeNumber = volumeNumber;
  }

  public String getVolume() {
    return volume;
  }

  public void setVolume(String volume) {
    this.volume = volume;
  }

  public String getIssue() {
    return issue;
  }

  public void setIssue(String issue) {
    this.issue = issue;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getPublisherLocation() {
    return publisherLocation;
  }

  public void setPublisherLocation(String publisherLocation) {
    this.publisherLocation = publisherLocation;
  }

  public String getPublisherName() {
    return publisherName;
  }

  public void setPublisherName(String publisherName) {
    this.publisherName = publisherName;
  }

  public String getPages() {
    return pages;
  }

  public void setPages(String pages) {
    this.pages = pages;
  }

  public String geteLocationID() {
    return eLocationID;
  }

  public void seteLocationID(String eLocationID) {
    this.eLocationID = eLocationID;
  }

  public String getJournal() {
    return journal;
  }

  public void setJournal(String journal) {
    this.journal = journal;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public List<String> getCollaborativeAuthors() {
    return collaborativeAuthors;
  }

  public void setCollaborativeAuthors(List<String> collaborativeAuthors) {
    this.collaborativeAuthors = collaborativeAuthors;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getDoi() {
    return doi;
  }

  public void setDoi(String doi) {
    this.doi = doi;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getCitationType() {
    return citationType;
  }

  public void setCitationType(String citationType) {
    this.citationType = citationType;
  }

  public List<CitedArticlePerson> getAuthors() {
    return authors;
  }

  public void setAuthors(List<CitedArticlePerson> authors) {
    this.authors = authors;
  }

  public List<CitedArticlePerson> getEditors() {
    return editors;
  }

  public void setEditors(List<CitedArticlePerson> editors) {
    this.editors = editors;
  }

}
