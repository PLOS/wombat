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

/**
 * Represent the Author or Editor of a reference.
 *
 */
public class ReferencePerson {
  private String fullName;
  private String givenNames;
  private String surname;
  private String suffix;

  private ReferencePerson(Builder builder) {
    this.fullName = builder.fullName;
    this.givenNames = builder.givenNames;
    this.surname = builder.surname;
    this.suffix = builder.suffix;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getGivenNames() {
    return givenNames;
  }

  public void setGivenNames(String givenNames) {
    this.givenNames = givenNames;
  }

  public String getSurname() {
    return surname;
  }

  public void setSurname(String surname) {
    this.surname = surname;
  }

  public String getSuffix() {
    return suffix;
  }

  public void setSuffix(String suffix) {
    this.suffix = suffix;
  }

  public static class Builder {
    private String fullName;
    private String givenNames;
    private String surname;
    private String suffix;

    public Builder(String fullName) {
      this.fullName = fullName;
    }

    public Builder givenNames(String givenNames) {
      this.givenNames = givenNames;
      return this;
    }

    public Builder surname(String surname) {
      this.surname = surname;
      return this;
    }

    public Builder suffix(String suffix) {
      this.suffix = suffix;
      return this;
    }

    public ReferencePerson build() {
      return new ReferencePerson(this);
    }
  }


}
