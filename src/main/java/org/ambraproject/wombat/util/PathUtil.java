package org.ambraproject.wombat.util;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

public class PathUtil {
  private PathUtil() {
    throw new AssertionError("Not instantiable");
  }

  public static final Splitter SPLITTER = Splitter.on('/').omitEmptyStrings();
  public static final Joiner JOINER = Joiner.on('/');

}
