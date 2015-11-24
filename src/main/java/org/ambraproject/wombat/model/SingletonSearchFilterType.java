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
