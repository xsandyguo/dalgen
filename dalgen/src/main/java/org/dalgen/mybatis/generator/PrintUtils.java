package org.dalgen.mybatis.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.dalgen.mybatis.provider.db.table.Table;

public class PrintUtils {

  public static void printExceptionsSumary(String msg, String outRoot, List<Exception> exceptions)
      throws FileNotFoundException {
    File errorFile = new File(outRoot, "generator_error.log");
    if (exceptions != null && exceptions.size() > 0) {
      System.err.println("[Generate Error Summary] : " + msg);
      errorFile.getParentFile().mkdirs();
      PrintStream output = new PrintStream(new FileOutputStream(errorFile));
      for (int i = 0; i < exceptions.size(); i++) {
        Exception e = exceptions.get(i);
        System.err.println("[GENERATE ERROR]:" + e);
        if (i == 0) {
          e.printStackTrace();
        }
        e.printStackTrace(output);
      }
      output.close();
      System.err.println("***************************************************************");
      System.err.println("* " + "* 输出目录已经生成generator_error.log用于查看错误 ");
      System.err.println("***************************************************************");
    }
  }

  public static void printBeginProcess(String displayText, boolean isDatele) {
    System.out.println("***************************************************************");
    System.out.println("* BEGIN " + (isDatele ? " delete by " : " generate by ") + displayText);
    System.out.println("***************************************************************");
  }

  public static void printAllTableNames(List<Table> tables) throws Exception {
    System.out.println("\n----All TableNames BEGIN----");
    for (int i = 0; i < tables.size(); i++) {
      String sqlName = ((Table) tables.get(i)).getSqlName();
      System.out.println(sqlName);
    }
    System.out.println("----All TableNames END----");
  }
}
