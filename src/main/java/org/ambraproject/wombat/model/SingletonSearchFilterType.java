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

public enum SingletonSearchFilterType implements SearchFilterType {
  SUBJECT_AREA {
    @Override
    public String getFilterValue(String value) {
      return value;
    }

    @Override
    public String getParameterName() {
      return "filterSubjects";
    }

    @Override
    public String getFilterMapKey() {
      return "subject_area";
    }
  },

  AUTHOR {
    @Override
    public String getFilterValue(String value) {
      return value;
    }

    @Override
    public String getParameterName() {
      return "filterAuthors";
    }

    @Override
    public String getFilterMapKey() {
      return "author";
    }
  },

  ARTICLE_TYPE {

    @Override
    public String getFilterValue(String value) {
      return value;
    }

    @Override
    public String getParameterName() {
      return "filterArticleTypes";
    }

    @Override
    public String getFilterMapKey() {
      return "article_type";
    }
  },

  SECTION {

    @Override
    public String getFilterValue(String value) {
      return value;
    }

    @Override
    public String getParameterName() {
      return "filterSections";
    }

    @Override
    public String getFilterMapKey() {
      return "section";
    }
  };
}
