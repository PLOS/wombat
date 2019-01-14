package org.ambraproject.wombat.util; 

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {

  public static String read(URI fileuri) {
    try {
      Path path = Paths.get(fileuri);
            
      Stream<String> lines = Files.lines(path);
      String content = lines.collect(Collectors.joining("\n"));
      lines.close();

      return content;
    }
    catch (java.io.IOException e) {
      throw new RuntimeException(e);
    }
  }

  // read file from classpath
  public static String read(String file) {
    try {
      return read(FileUtils.class.getClassLoader().getResource(file).toURI());
    }
    catch (java.net.URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public static String read(File file) {
    return read(file.toURI());
  }
}
