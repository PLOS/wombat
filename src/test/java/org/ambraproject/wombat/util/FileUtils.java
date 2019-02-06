package org.ambraproject.wombat.util; 

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

  public static File getFile(String file) {
    try {
      return new File(FileUtils.class.getClassLoader().getResource(file).toURI());
    }
    catch (java.net.URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public static void serialize(Object obj, File file) {

    try (FileOutputStream   fos = new FileOutputStream(file);
         ObjectOutputStream oos = new ObjectOutputStream(fos))
    {
      oos.writeObject(obj);
    }
    catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  public static Object deserialize(File file) {

    try (FileInputStream   fis = new FileInputStream(file);
         ObjectInputStream ois = new ObjectInputStream(fis);)
    {
      return ois.readObject();      
    }
    catch (IOException|ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
