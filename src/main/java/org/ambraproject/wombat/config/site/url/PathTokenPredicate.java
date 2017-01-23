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
import org.ambraproject.wombat.util.PathUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;

class PathTokenPredicate implements SiteRequestPredicate {

  private final String token;

  PathTokenPredicate(String token) {
    Preconditions.checkArgument(!token.isEmpty());
    this.token = token;
  }

  @Override
  public boolean isForSite(HttpServletRequest request) {
    Iterator<String> path = PathUtil.SPLITTER.split(request.getServletPath()).iterator();
    return path.hasNext() && token.toLowerCase().equals(path.next().toLowerCase());
  }

  @Override
  public String toString() {
    return "PathTokenPredicate{" +
        "token='" + token + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    return token.equals(((PathTokenPredicate) o).token);
  }

  @Override
  public int hashCode() {
    return token.hashCode();
  }

}
