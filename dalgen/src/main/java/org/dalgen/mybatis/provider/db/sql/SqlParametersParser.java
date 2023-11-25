package org.dalgen.mybatis.provider.db.sql;

import java.util.*;

import org.dalgen.mybatis.provider.db.table.Column;
import org.dalgen.mybatis.provider.db.table.NotFoundTableException;
import org.dalgen.mybatis.provider.db.table.Table;
import org.dalgen.mybatis.sqlparse.NameWithAlias;
import org.dalgen.mybatis.sqlparse.ParsedSql;
import org.dalgen.mybatis.sqlparse.SqlParseHelper;
import org.dalgen.mybatis.typemapping.JdbcType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SqlParametersParser {
  private static Map<String, Column> specialParametersMapping = new HashMap<String, Column>();
  public LinkedHashSet<SqlParameter> params                   = new LinkedHashSet<SqlParameter>();
  public List<SqlParameter>          allParams                = new ArrayList<SqlParameter>();

  {
    specialParametersMapping.put("offset", new Column(null, JdbcType.INTEGER.TYPE_CODE, "INTEGER",
        "offset", 0, 0, false, false, false, false, null, null));
    specialParametersMapping.put("limit", new Column(null, JdbcType.INTEGER.TYPE_CODE, "INTEGER",
        "limit", 0, 0, false, false, false, false, null, null));
    specialParametersMapping.put("pageSize", new Column(null, JdbcType.INTEGER.TYPE_CODE, "INTEGER",
        "pageSize", 0, 0, false, false, false, false, null, null));
    specialParametersMapping.put("pageNo", new Column(null, JdbcType.INTEGER.TYPE_CODE, "INTEGER",
        "pageNo", 0, 0, false, false, false, false, null, null));
    specialParametersMapping.put("pageNumber", new Column(null, JdbcType.INTEGER.TYPE_CODE,
        "INTEGER", "pageNumber", 0, 0, false, false, false, false, null, null));
    specialParametersMapping.put("pageNum", new Column(null, JdbcType.INTEGER.TYPE_CODE, "INTEGER",
        "pageNumber", 0, 0, false, false, false, false, null, null));
    specialParametersMapping.put("page", new Column(null, JdbcType.INTEGER.TYPE_CODE, "INTEGER",
        "page", 0, 0, false, false, false, false, null, null));

    specialParametersMapping.put("beginRow", new Column(null, JdbcType.INTEGER.TYPE_CODE, "INTEGER",
        "beginRow", 0, 0, false, false, false, false, null, null));
    specialParametersMapping.put("beginRows", new Column(null, JdbcType.INTEGER.TYPE_CODE,
        "INTEGER", "beginRows", 0, 0, false, false, false, false, null, null));
    specialParametersMapping.put("startRow", new Column(null, JdbcType.INTEGER.TYPE_CODE, "INTEGER",
        "startRow", 0, 0, false, false, false, false, null, null));
    specialParametersMapping.put("startRows", new Column(null, JdbcType.INTEGER.TYPE_CODE,
        "INTEGER", "startRows", 0, 0, false, false, false, false, null, null));
    specialParametersMapping.put("endRow", new Column(null, JdbcType.INTEGER.TYPE_CODE, "INTEGER",
        "endRow", 0, 0, false, false, false, false, null, null));
    specialParametersMapping.put("endRows", new Column(null, JdbcType.INTEGER.TYPE_CODE, "INTEGER",
        "endRows", 0, 0, false, false, false, false, null, null));
    specialParametersMapping.put("lastRow", new Column(null, JdbcType.INTEGER.TYPE_CODE, "INTEGER",
        "lastRow", 0, 0, false, false, false, false, null, null));
    specialParametersMapping.put("lastRows", new Column(null, JdbcType.INTEGER.TYPE_CODE, "INTEGER",
        "lastRows", 0, 0, false, false, false, false, null, null));

    specialParametersMapping.put("orderBy", new Column(null, JdbcType.VARCHAR.TYPE_CODE, "VARCHAR",
        "orderBy", 0, 0, false, false, false, false, null, null));
    specialParametersMapping.put("orderby", new Column(null, JdbcType.VARCHAR.TYPE_CODE, "VARCHAR",
        "orderby", 0, 0, false, false, false, false, null, null));
    specialParametersMapping.put("sortColumns", new Column(null, JdbcType.VARCHAR.TYPE_CODE,
        "VARCHAR", "sortColumns", 0, 0, false, false, false, false, null, null));
  }

  public void execute(ParsedSql parsedSql, Sql sql) throws Exception {
    long start = System.currentTimeMillis();
    for (int i = 0; i < parsedSql.getParameterNames().size(); i++) {
      String paramName = parsedSql.getParameterNames().get(i);
      Column column = findColumnByParamName(parsedSql, sql, paramName);
      if (column == null) {
        column = specialParametersMapping.get(paramName);
        if (column == null) {
          // FIXME 不能猜测的column类型
          column = new Column(null, JdbcType.UNDEFINED.TYPE_CODE, "UNDEFINED", paramName, 0, 0,
              false, false, false, false, null, null);
        }
      }
      SqlParameter param = new SqlParameter(column);

      param.setParamName(paramName);
      if (isMatchListParam(sql.getSourceSql(), paramName)) { // FIXME
                                                             // 只考虑(:username)未考虑(#inUsernames#) and
                                                             // (#{inPassword}),并且可以使用
        // #inUsername[]#
        param.setListParam(true);
      }
      params.add(param);
      allParams.add(param);
    }
    log.info("parseForSqlParameters() cost:" + (System.currentTimeMillis() - start));
  }

  public boolean isMatchListParam(String sql, String paramName) {
    return sql.matches("(?s).*\\sin\\s*\\([:#\\$&]\\{?" + paramName + "\\}?[$#}]?\\).*") // match in
                                                                                         // (:username)
                                                                                         // ,not in
                                                                                         // (#username#)
        || sql.matches("(?s).*[#$]" + paramName + "\\[]\\.?\\w*[#$].*") // match #user[]# $user[]$
                                                                        // #user[].age# for ibatis
        || sql.matches("(?s).*[#$]\\{" + paramName + "\\[[$\\{\\}\\w]+]\\}*.*"); // match
                                                                                 // #{user[index]}#
                                                                                 // ${user[${index}]}
                                                                                 // for
    // mybatis
  }

  private Column findColumnByParamName(ParsedSql parsedSql, Sql sql, String paramName)
      throws Exception {
    Column column = sql.getColumnByName(paramName);
    if (column == null) {
      // FIXME 还未处理 t.username = :username的t前缀问题,应该直接根据 t.确定属于那一张表,不需要再猜测
      String leftColumn =
          SqlParseHelper.getColumnNameByRightCondition(parsedSql.toString(), paramName);
      if (leftColumn != null) {
        column = findColumnByParseSql(parsedSql, leftColumn);
      }
    }
    if (column == null) {
      column = findColumnByParseSql(parsedSql, paramName);
    }
    return column;
  }

  private Column findColumnByParseSql(ParsedSql sql, String paramName) throws Exception {
    if (paramName == null) {
      throw new NullPointerException("'paramName' must be not null");
    }
    try {
      Collection<NameWithAlias> tableNames = SqlParseHelper.getTableNamesByQuery(sql.toString());
      for (NameWithAlias tableName : tableNames) {
        Table t = SqlFactory.getTableFromCache(tableName.getName());
        if (t != null) {
          Column column = t.getColumnByName(paramName);
          if (column != null) {
            return column;
          }
        }
      }
    } catch (NotFoundTableException e) {
      throw new IllegalArgumentException("get tableNamesByQuery occer error:" + sql.toString(), e);
    }
    return null;
  }
}
