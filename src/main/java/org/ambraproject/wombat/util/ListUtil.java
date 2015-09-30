package org.ambraproject.wombat.util;

import java.util.List;

public class ListUtil {
  private ListUtil() {
    throw new AssertionError("Not instantiable");
  }

  public static boolean isNullOrEmpty(List list) {
    return list == null || list.isEmpty();
  }

}
