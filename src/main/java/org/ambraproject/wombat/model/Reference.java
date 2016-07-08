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

public class Reference {

  private Integer year;
  private String month;
  private String day;
  private Integer volumeNumber;
  private String volume;
  private String issue;
  private String title;
  private String publisherName;
  private String publicationType;
  private String fPage;
  private String lPage;
  private String journal;
  private String url;
  private String doi;
  private List<ReferencePerson> authors;
  private List<ReferencePerson> editors;

  public static enum PublicationType {
    ARTICLE ("Article"),
    BOOK ("Book"),
    MISC ("Misc");

    private String value;

    private PublicationType(String publicationTye) {
      this.value = publicationTye;
    }

    public String getValue() {
      return value;
    }

    public String toString() {
      return String.format("http://purl.org/net/nknouf/ns/bibtex# %s", value);
    }
  };

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
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

  public String getPublisherName() {
    return publisherName;
  }

  public void setPublisherName(String publisherName) {
    this.publisherName = publisherName;
  }

  public String getPublicationType() {
    return publicationType;
  }

  public void setPublicationType(String publicationType) {
    this.publicationType = publicationType;
  }

  public String getfPage() {
    return fPage;
  }

  public void setfPage(String fPage) {
    this.fPage = fPage;
  }

  public String getlPage() {
    return lPage;
  }

  public void setlPage(String lPage) {
    this.lPage = lPage;
  }

  public String getJournal() {
    return journal;
  }

  public void setJournal(String journal) {
    this.journal = journal;
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

  public List<ReferencePerson> getAuthors() {
    return authors;
  }

  public void setAuthors(List<ReferencePerson> authors) {
    this.authors = authors;
  }

  public List<ReferencePerson> getEditors() {
    return editors;
  }

  public void setEditors(List<ReferencePerson> editors) {
    this.editors = editors;
  }

}
