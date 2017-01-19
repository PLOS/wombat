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

package org.ambraproject.wombat.config.site.url;

import com.google.common.base.Preconditions;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

class HeaderPredicate implements SiteRequestPredicate {

  private final String headerName;
  private final String requiredValue;

  HeaderPredicate(String headerName, String requiredValue) {
    this.headerName = Preconditions.checkNotNull(headerName);
    this.requiredValue = Preconditions.checkNotNull(requiredValue);
  }

  @Override
  public boolean isForSite(HttpServletRequest request) {
    Enumeration headers = request.getHeaders(headerName);
    while (headers.hasMoreElements()) {
      if (requiredValue.equals(headers.nextElement())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return "HeaderPredicate{" +
        "headerName='" + headerName + '\'' +
        ", requiredValue='" + requiredValue + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    HeaderPredicate that = (HeaderPredicate) o;

    if (!headerName.equals(that.headerName)) return false;
    if (!requiredValue.equals(that.requiredValue)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = headerName.hashCode();
    result = 31 * result + requiredValue.hashCode();
    return result;
  }

}
