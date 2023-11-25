package org.dalgen.mybatis.ext.tableconfig;

import org.dalgen.mybatis.util.StringHelper;

public class Helper {

  public static String getReplacement(String open, String prepend) {
    String replacement = null;
    if (prepend != null) {
      replacement = " " + prepend + " " + StringHelper.defaultString(open);
    } else {
      if (StringHelper.isEmpty(open)) {
        replacement = "";
      } else {
        replacement = (" " + open);
      }
    }
    return replacement;
  }

  public static String removeSelectKeyXmlForInsertSql(String str) {
    if (str == null)
      return null;
    return str.replaceAll("(?s)<selectKey.*?>.*</selectKey>", "");
  }

  public static String removeComments(String str) {
    if (str == null)
      return null;
    str = str.replaceAll("(?s)<!--.*?-->", "").replaceAll("(?s)/\\*.*?\\*/", "")
        .replace("query not allowed", "");
    return str;
  }
}
