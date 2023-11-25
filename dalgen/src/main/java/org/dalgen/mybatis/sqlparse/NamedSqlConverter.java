package org.dalgen.mybatis.sqlparse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dalgen.mybatis.util.StringHelper;

/**
 * 将sql从占位符转换为命名参数,如 select * from user where id =? ,将返回: select * from user where id = #id#
 *
 * @param includeSqls
 * @param prefix 命名参数的前缀
 * @param suffix 命名参数的后缀
 * @return
 */
public class NamedSqlConverter {
  private String prefix;
  private String suffix;

  public NamedSqlConverter(String prefix, String suffix) {
    if (prefix == null)
      throw new NullPointerException("'prefix' must be not null");
    if (suffix == null)
      throw new NullPointerException("'suffix' must be not null");
    this.prefix = prefix;
    this.suffix = suffix;
  }

  private static Matcher matcher(String input, int flags, String... regexArray) {
    for (String regex : regexArray) {
      Pattern p = Pattern.compile(regex, flags);
      Matcher m = p.matcher(input);
      if (m.find()) {
        return m;
      }
    }
    return null;
  }

  public String convert2NamedParametersSql(String sql) {
    if (sql.trim().toLowerCase().matches("(?is)\\s*insert\\s+into\\s+.*")) {
      return replace2NamedParameters(replaceInsertSql2NamedParameters(sql));
    } else {
      return replace2NamedParameters(sql);
    }
  }

  private String replace2NamedParameters(String sql) {
    String replacedSql = replace2NamedParametersByOperator(sql, "[=<>!]{1,2}"); // 缺少oracle的^=运算符:
    // !=,<>,^=:不等于
    replacedSql = replace2NamedParametersByOperator(replacedSql, "\\s+like\\s+"); // like
    // replacedSql =
    // replace2NamedParametersByOperator(replacedSql,"\\s+not\\s+in\\s+\\("); // not in
    // replacedSql = replace2NamedParametersByOperator(replacedSql,"\\s+in\\s+\\("); //
    // in
    return replacedSql;
  }

  private String replaceInsertSql2NamedParameters(String sql) {
    if (sql.matches("(?is)\\s*insert\\s+into\\s+\\w+\\s+values\\s*\\(.*\\).*")) {
      if (sql.indexOf("?") >= 0) {
        throw new IllegalArgumentException("无法解析的insert sql:" + sql + ",values()段没有包含疑问号?");
      } else {
        return sql;
      }
    }
    // FIXME: insert into user_info(user,pwd) values (length(?),?);
    // 将没有办法解析,因为正则表达式由于length()函数匹配错误.
    // 需要处理 <selectKey>问题
    String selectKeyPattern = "<selectKey.*";
    String insertPattern = "\\s*insert\\s+into.*\\((.*?)\\).*values.*?\\((.*)\\).*";
    Matcher m = matcher(sql, Pattern.DOTALL | Pattern.CASE_INSENSITIVE,
        insertPattern + selectKeyPattern, insertPattern);
    if (m != null) {
      String[] columns = StringHelper.tokenizeToStringArray(m.group(1), ", \t\n\r\f");
      String[] values = StringHelper.tokenizeToStringArray(m.group(2), ", \t\n\r\f");
      if (columns.length != values.length) {
        throw new IllegalArgumentException("insert 语句的插入列与值列数目不相等,sql:" + sql + " \ncolumns:"
            + StringHelper.join(columns, ",") + " \nvalues:" + StringHelper.join(values, ","));
      }
      for (int i = 0; i < columns.length; i++) {
        String column = columns[i];
        String paranName =
            StringHelper.uncapitalize(StringHelper.makeAllWordFirstLetterUpperCase(column));
        values[i] = values[i].replace("?", prefix + paranName + suffix);;
      }
      return StringHelper.replace(m.start(2), m.end(2), sql, StringHelper.join(values, ","));
    }
    throw new IllegalArgumentException("无法解析的sql:" + sql + ",不匹配正则表达式:" + insertPattern);
  }

  private String replace2NamedParametersByOperator(String sql, String operator) {
    Pattern p = Pattern.compile("(\\w+)\\s*" + operator + "\\s*\\?",
        Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(sql);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      String segment = m.group(0);
      String columnSqlName = m.group(1);
      String paramName =
          StringHelper.uncapitalize(StringHelper.makeAllWordFirstLetterUpperCase(columnSqlName));
      String replacedSegment = segment.replace("?", prefix + paramName + suffix);
      m.appendReplacement(sb, replacedSegment);
    }
    m.appendTail(sb);
    return sb.toString();
  }
}
