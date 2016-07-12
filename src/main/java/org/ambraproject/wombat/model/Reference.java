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

  private String title;
  private Integer year;
  private String journal;
  private String volume;
  private Integer volumeNumber;
  private String issue;
  private String publisherName;
  private String isbn;
  private String fPage;
  private String lPage;
  private String doi;
  private String uri;
  private List<NlmPerson> authors;
  private List<String> collabAuthors;
  private String unStructuredReference;

  public static enum PublicationType {
    JOURNAL ("journal"),
    BOOK ("book"),
    OTHER("other");

    private String value;

    private PublicationType(String publicationTye) {
      this.value = publicationTye;
    }

    public String getValue() {
      return value;
    }


  };

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
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

  public String getUnStructuredReference() {
    return unStructuredReference;
  }

  public void setUnStructuredReference(String unStructuredReference) {
    this.unStructuredReference = unStructuredReference;
  }

  public String getPublisherName() {
    return publisherName;
  }

  public void setPublisherName(String publisherName) {
    this.publisherName = publisherName;
  }

  public String getIsbn() {
    return isbn;
  }

  public void setIsbn(String isbn) {
    this.isbn = isbn;
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

  public String getDoi() {
    return doi;
  }

  public void setDoi(String doi) {
    this.doi = doi;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public List<NlmPerson> getAuthors() {
    return authors;
  }

  public void setAuthors(List<NlmPerson> authors) {
    this.authors = authors;
  }

  public List<String> getCollabAuthors() {
    return collabAuthors;
  }

  public void setCollabAuthors(List<String> collabAuthors) {
    this.collabAuthors = collabAuthors;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Reference reference = (Reference) o;

    if (authors != null ? !authors.equals(reference.authors) : reference.authors != null) return false;
    if (doi != null ? !doi.equals(reference.doi) : reference.doi != null) return false;
    if (fPage != null ? !fPage.equals(reference.fPage) : reference.fPage != null) return false;
    if (issue != null ? !issue.equals(reference.issue) : reference.issue != null) return false;
    if (journal != null ? !journal.equals(reference.journal) : reference.journal != null) return false;
    if (lPage != null ? !lPage.equals(reference.lPage) : reference.lPage != null) return false;
    if (publisherName != null ? !publisherName.equals(reference.publisherName) : reference.publisherName != null)
      return false;
    if (title != null ? !title.equals(reference.title) : reference.title != null) return false;
    if (volume != null ? !volume.equals(reference.volume) : reference.volume != null) return false;
    if (volumeNumber != null ? !volumeNumber.equals(reference.volumeNumber) : reference.volumeNumber != null)
      return false;
    if (year != null ? !year.equals(reference.year) : reference.year != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = year != null ? year.hashCode() : 0;
    result = 31 * result + (volumeNumber != null ? volumeNumber.hashCode() : 0);
    result = 31 * result + (volume != null ? volume.hashCode() : 0);
    result = 31 * result + (issue != null ? issue.hashCode() : 0);
    result = 31 * result + (title != null ? title.hashCode() : 0);
    result = 31 * result + (publisherName != null ? publisherName.hashCode() : 0);
    result = 31 * result + (fPage != null ? fPage.hashCode() : 0);
    result = 31 * result + (lPage != null ? lPage.hashCode() : 0);
    result = 31 * result + (journal != null ? journal.hashCode() : 0);
    result = 31 * result + (doi != null ? doi.hashCode() : 0);
    result = 31 * result + (authors != null ? authors.hashCode() : 0);
    return result;
  }
}
