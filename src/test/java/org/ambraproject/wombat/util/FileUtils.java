package org.ambraproject.wombat.util; 

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import com.google.common.io.Resources;

public class FileUtils {
  public static String read(String path) throws IOException {
    return Resources.toString(Resources.getResource(path), StandardCharsets.UTF_8);
  }
}
