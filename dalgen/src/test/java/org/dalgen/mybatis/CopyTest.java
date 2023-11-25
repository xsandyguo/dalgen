package org.dalgen.mybatis;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class CopyTest {
  public static void main(String[] args) throws IOException {
    JarFile jarFile = new JarFile(URLDecoder.decode(
        "/Users/john/.m2/repository/org/dalgen/mybatis/dalgen-maven-plugin/1.0/dalgen-maven-plugin-1.0.jar",
        StandardCharsets.UTF_8));


    Enumeration<JarEntry> entries = jarFile.entries();

    while (entries.hasMoreElements()) {
      JarEntry jarEntry = entries.nextElement();
      String fileName = jarEntry.getName();

      System.out.println("file:" + fileName);
    }
  }
}
