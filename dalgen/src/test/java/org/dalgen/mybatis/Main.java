package org.dalgen.mybatis;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.dalgen.mybatis.bootstrap.AutoGen;
import org.dalgen.mybatis.bootstrap.ExecuteTarget;

/** The type Main. */
public class Main {
  /**
   * The entry point of application.
   *
   * @param args the input arguments
   * @throws Exception the exception
   */
  public static void main(String[] args) throws Exception {
    System.setProperty("tableName", "member");
    System.setProperty("cmdName", "dal");

    String executeTarget = System.getProperty("cmdName");
    if (StringUtils.isBlank(executeTarget)) {
      printUsage();
      return;
    }

    String tableName = System.getProperty("tableName");
    if (StringUtils.isBlank(executeTarget)) {
      printUsage();
      return;
    }

    System.out.println(MessageFormat.format("cmd:{0} table:{1}", executeTarget, tableName));

    AutoGen generator = new AutoGen(tableName);
    if (executeTarget.equalsIgnoreCase(ExecuteTarget.dal.name())) {
      generator.dal();
    } else if (executeTarget.equalsIgnoreCase(ExecuteTarget.table.name())) {
      generator.table();
    } else {
      System.out.println(MessageFormat.format("unkown cmd:", executeTarget));
    }

    System.out.println("---------------------Generator executed SUCCESS---------------------");
  }

  private static void printUsage() {
    System.out.println("dalgen cmd table");
  }
}
