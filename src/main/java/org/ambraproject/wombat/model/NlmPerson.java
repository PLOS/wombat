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
