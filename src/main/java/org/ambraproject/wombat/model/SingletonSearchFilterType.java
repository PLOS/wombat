package org.ambraproject.wombat.model;

public enum SingletonSearchFilterType implements SearchFilterType {
  //todo: add enum entries for singleton filter types such as author and dates
  SUBJECT_AREA {
    @Override
    public String getFilterValue(String value) {
      return value;
    }

    @Override
    public String getParameterName() {
      return "filterSubjects";
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
  }

}
