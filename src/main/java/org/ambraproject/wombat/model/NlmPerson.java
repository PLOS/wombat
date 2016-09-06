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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;

/**
 * Represent the Author or Editor of a reference.
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class NlmPerson implements Serializable {
  private String fullName;
  private String givenNames;
  private String surname;
  private String suffix;

  /**
   * @deprecated required for JAXB serializer
   */
  @Deprecated
  public NlmPerson() {
  }

  private NlmPerson(Builder builder) {
    this.fullName = builder.fullName;
    this.givenNames = builder.givenNames;
    this.surname = builder.surname;
    this.suffix = builder.suffix;
  }

  public String getFullName() {
    return fullName;
  }

  public String getGivenNames() {
    return givenNames;
  }

  public String getSurname() {
    return surname;
  }


  public String getSuffix() {
    return suffix;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String fullName;
    private String givenNames;
    private String surname;
    private String suffix;

    public Builder() {
    }

    public Builder setFullName(String fullName) {
      this.fullName = fullName;
      return this;
    }

    public Builder setGivenNames(String givenNames) {
      this.givenNames = givenNames;

      return this;
    }

    public Builder setSurname(String surname) {
      this.surname = surname;
      return this;
    }

    public Builder setSuffix(String suffix) {
      this.suffix = suffix;
      return this;
    }

    public NlmPerson build() {
      return new NlmPerson(this);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Builder builder = (Builder) o;

    if (fullName != null ? !fullName.equals(builder.fullName) : builder.fullName != null) return false;
    if (givenNames != null ? !givenNames.equals(builder.givenNames) : builder.givenNames != null)
      return false;
    if (suffix != null ? !suffix.equals(builder.suffix) : builder.suffix != null) return false;
    if (surname != null ? !surname.equals(builder.surname) : builder.surname != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = fullName != null ? fullName.hashCode() : 0;
    result = 31 * result + (givenNames != null ? givenNames.hashCode() : 0);
    result = 31 * result + (surname != null ? surname.hashCode() : 0);
    result = 31 * result + (suffix != null ? suffix.hashCode() : 0);
    return result;
  }

}
