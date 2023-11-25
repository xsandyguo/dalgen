package org.dalgen.mybatis.util;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;


/**
 * The type Conf init.
 */
public class ConfInit {

  /**
   * The constant NEED_COPY_TEMPLATES.
   */
  public static final String NEED_COPY_TEMPLATES = "templates/";


  private static JarFile     jarFile;

  static {
    try {
      jarFile = new JarFile(URLDecoder.decode(
          ConfInit.class.getProtectionDomain().getCodeSource().getLocation().getPath(),
          StandardCharsets.UTF_8));
    } catch (IOException e) {
    }
  }


  /**
   * copy template 文件
   *
   * @param outDir the out dir
   */
  public static void copyTemplateFile(String outDir) {
    try {
      doCopyTemplateFile(outDir);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Do copy template file.
   *
   * @param outDir the out dir
   * @throws IOException the io exception
   */
  public static void doCopyTemplateFile(String outDir) throws IOException {
    Enumeration<JarEntry> entries = jarFile.entries();
    while (entries.hasMoreElements()) {
      JarEntry jarEntry = entries.nextElement();
      if (jarEntry.isDirectory()) {
        continue;
      }
      String fileName = jarEntry.getName();
      if (!StringUtils.startsWith(fileName, NEED_COPY_TEMPLATES)) {
        continue;
      }

      copyAndOverWriteFile(fileName,
          new File(outDir + fileName.substring(NEED_COPY_TEMPLATES.length() - 1)));
    }
  }

  /**
   * Copy and over write file.
   *
   * @param sourceName the soure name
   * @param outFile the out file
   * @throws IOException the io exception
   */
  private static void copyAndOverWriteFile(String sourceName, File outFile) throws IOException {
    FileUtils.forceMkdir(outFile.getParentFile());

    if (StringUtils.indexOf(sourceName, '.') == -1) {
      return;
    }
    BufferedReader reader = null;
    BufferedWriter writer = null;
    try {
      reader = new BufferedReader(new InputStreamReader(
          ConfInit.class.getResourceAsStream("/" + sourceName), StandardCharsets.UTF_8));
      writer = new BufferedWriter(
          new OutputStreamWriter(new FileOutputStream(outFile), StandardCharsets.UTF_8));
      String line;
      while ((line = reader.readLine()) != null) {
        writer.write(line);
        writer.write("\n");
      }
      writer.flush();

      System.out.println("release:" + outFile);
    } catch (Exception e) {
      System.out.println("======");
    } finally {
      IOUtils.closeQuietly(reader);
      IOUtils.closeQuietly(writer);
    }
  }

}
