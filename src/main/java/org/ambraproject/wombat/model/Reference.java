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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

@XmlRootElement(name = "reference")
@XmlAccessorType(XmlAccessType.FIELD)
public class Reference implements Serializable {


  private String title;
  private String chapterTitle;
  private Integer year;
  private String journal;
  private String fullArticleLink;
  private String volume;
  private Integer volumeNumber;
  private String issue;
  private String publisherName;
  private String isbn;
  private String fPage;
  private String lPage;
  private String doi;
  private String uri;
  private ImmutableList<NlmPerson> authors;
  private ImmutableList<String> collabAuthors;
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

  private Reference() {

  }
  private Reference(Builder builder) {
    this.title = builder.title;
    this.chapterTitle = builder.chapterTitle;
    this.year = builder.year;
    this.journal = builder.journal;
    this.fullArticleLink = builder.fullArticleLink;
    this.volume = builder.volume;
    this.volumeNumber = builder.volumeNumber;
    this.issue = builder.issue;
    this.publisherName = builder.publisherName;
    this.isbn = builder.isbn;
    this.fPage = builder.fPage;
    this.lPage = builder.lPage;
    this.doi = builder.doi;
    this.uri = builder.uri;
    this.authors = ImmutableList.copyOf(builder.authors);
    this.collabAuthors = ImmutableList.copyOf(builder.collabAuthors);
    this.unStructuredReference = builder.unStructuredReference;
  }

  public String getTitle() {
    return title;
  }

  public String getChapterTitle() {
    return chapterTitle;
  }

  public Integer getYear() {
    return year;
  }

  public String getJournal() {
    return journal;
  }

  public String getFullArticleLink() {
    return fullArticleLink;
  }

  public String getVolume() {
    return volume;
  }

  public Integer getVolumeNumber() {
    return volumeNumber;
  }

  public String getIssue() {
    return issue;
  }

  public String getPublisherName() {
    return publisherName;
  }

  public String getIsbn() {
    return isbn;
  }

  public String getfPage() {
    return fPage;
  }

  public String getlPage() {
    return lPage;
  }

  public String getDoi() {
    return doi;
  }

  public String getUri() {
    return uri;
  }

  public List<NlmPerson> getAuthors() {
    return authors;
  }

  public List<String> getCollabAuthors() {
    return collabAuthors;
  }

  public String getUnStructuredReference() {
    return unStructuredReference;
  }

  public static Builder build() {
    return new Builder();
  }

  public static class Builder {
    private String title;
    private String chapterTitle;
    private Integer year;
    private String journal;
    private String fullArticleLink;
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

    public Builder(){

    }

    public Builder(Reference reference) {
      this.title = reference.title;
      this.chapterTitle = reference.chapterTitle;
      this.year = reference.year;
      this.journal = reference.journal;
      this.fullArticleLink = reference.fullArticleLink;
      this.volume = reference.volume;
      this.volumeNumber = reference.volumeNumber;
      this.issue = reference.issue;
      this.publisherName = reference.publisherName;
      this.isbn = reference.isbn;
      this.fPage = reference.fPage;
      this.lPage = reference.lPage;
      this.doi = reference.doi;
      this.uri = reference.uri;
      this.authors = ImmutableList.copyOf(reference.authors);
      this.collabAuthors = ImmutableList.copyOf(reference.collabAuthors);
      this.unStructuredReference = reference.unStructuredReference;
    }

    public Builder setTitle(String title) {
      this.title = title;
      return this;
    }

    public Builder setChapterTitle(String chapterTitle) {
      this.chapterTitle = chapterTitle;
      return this;
    }

    public Builder setYear(Integer year) {
      this.year = year;
      return this;
    }

    public Builder setJournal(String journal) {
      this.journal = journal;
      return this;
    }

    public Builder setFullArticleLink(String fullArticleLink) {
      this.fullArticleLink = fullArticleLink;
      return this;
    }

    public Builder setVolume(String volume) {
      this.volume = volume;
      return this;
    }

    public Builder setVolumeNumber(Integer volumeNumber) {
      this.volumeNumber = volumeNumber;
      return this;
    }

    public Builder setIssue(String issue) {
      this.issue = issue;
      return this;
    }

    public Builder setPublisherName(String publisherName) {
      this.publisherName = publisherName;
      return this;
    }

    public Builder setIsbn(String isbn) {
      this.isbn = isbn;
      return this;
    }

    public Builder setfPage(String fPage) {
      this.fPage = fPage;
      return this;
    }

    public Builder setlPage(String lPage) {
      this.lPage = lPage;
      return this;
    }

    public Builder setDoi(String doi) {
      this.doi = doi;
      return this;
    }

    public Builder setUri(String uri) {
      this.uri = uri;
      return this;
    }

    public Builder setAuthors(List<NlmPerson> authors) {
      this.authors = authors;
      return this;
    }

    public Builder setCollabAuthors(List<String> collabAuthors) {
      this.collabAuthors = collabAuthors;
      return this;
    }

    public Builder setUnStructuredReference(String unStructuredReference) {
      this.unStructuredReference = unStructuredReference;
      return this;
    }

    public Reference build() {
      return new Reference(this);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Reference reference = (Reference) o;

    if (authors != null ? !authors.equals(reference.authors) : reference.authors != null) return false;
    if (chapterTitle != null ? !chapterTitle.equals(reference.chapterTitle) : reference.chapterTitle != null)
      return false;
    if (collabAuthors != null ? !collabAuthors.equals(reference.collabAuthors) : reference.collabAuthors != null)
      return false;
    if (doi != null ? !doi.equals(reference.doi) : reference.doi != null) return false;
    if (fPage != null ? !fPage.equals(reference.fPage) : reference.fPage != null) return false;
    if (isbn != null ? !isbn.equals(reference.isbn) : reference.isbn != null) return false;
    if (issue != null ? !issue.equals(reference.issue) : reference.issue != null) return false;
    if (journal != null ? !journal.equals(reference.journal) : reference.journal != null) return false;
    if (lPage != null ? !lPage.equals(reference.lPage) : reference.lPage != null) return false;
    if (publisherName != null ? !publisherName.equals(reference.publisherName) : reference.publisherName != null)
      return false;
    if (title != null ? !title.equals(reference.title) : reference.title != null) return false;
    if (unStructuredReference != null ? !unStructuredReference.equals(reference.unStructuredReference) : reference.unStructuredReference != null)
      return false;
    if (uri != null ? !uri.equals(reference.uri) : reference.uri != null) return false;
    if (volume != null ? !volume.equals(reference.volume) : reference.volume != null) return false;
    if (volumeNumber != null ? !volumeNumber.equals(reference.volumeNumber) : reference.volumeNumber != null)
      return false;
    if (year != null ? !year.equals(reference.year) : reference.year != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = title != null ? title.hashCode() : 0;
    result = 31 * result + (chapterTitle != null ? chapterTitle.hashCode() : 0);
    result = 31 * result + (year != null ? year.hashCode() : 0);
    result = 31 * result + (journal != null ? journal.hashCode() : 0);
    result = 31 * result + (volume != null ? volume.hashCode() : 0);
    result = 31 * result + (volumeNumber != null ? volumeNumber.hashCode() : 0);
    result = 31 * result + (issue != null ? issue.hashCode() : 0);
    result = 31 * result + (publisherName != null ? publisherName.hashCode() : 0);
    result = 31 * result + (isbn != null ? isbn.hashCode() : 0);
    result = 31 * result + (fPage != null ? fPage.hashCode() : 0);
    result = 31 * result + (lPage != null ? lPage.hashCode() : 0);
    result = 31 * result + (doi != null ? doi.hashCode() : 0);
    result = 31 * result + (uri != null ? uri.hashCode() : 0);
    result = 31 * result + (authors != null ? authors.hashCode() : 0);
    result = 31 * result + (collabAuthors != null ? collabAuthors.hashCode() : 0);
    result = 31 * result + (unStructuredReference != null ? unStructuredReference.hashCode() : 0);
    return result;
  }
}
