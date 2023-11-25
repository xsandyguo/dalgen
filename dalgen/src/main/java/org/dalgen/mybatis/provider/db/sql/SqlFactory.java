package org.dalgen.mybatis.provider.db.sql;

import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dalgen.mybatis.provider.db.DataSourceProvider;
import org.dalgen.mybatis.provider.db.table.Table;
import org.dalgen.mybatis.provider.db.table.TableFactory;
import org.dalgen.mybatis.sqlerrorcode.SQLErrorCodeSQLExceptionTranslator;
import org.dalgen.mybatis.sqlparse.*;
import org.dalgen.mybatis.util.DBHelper;
import org.dalgen.mybatis.util.StringHelper;

import lombok.extern.slf4j.Slf4j;

/**
 * 根据SQL语句生成Sql对象,用于代码生成器的生成<br>
 * 示例使用：
 *
 * <pre>
 * Sql sql = new SqlFactory()
 *     .parseSql("select * from user_info where username=#username# and password=#password#");
 * </pre>
 *
 * @author badqiu
 */
@Slf4j
public class SqlFactory {

  public static Map<String, Table> cache = new HashMap<String, Table>();

  public SqlFactory() {}

  public static Table getTableFromCache(String tableSqlName) {
    if (tableSqlName == null) {
      throw new IllegalArgumentException("tableSqlName must be not null");
    }

    Table table = cache.get(tableSqlName.toLowerCase());
    if (table == null) {
      table = TableFactory.getInstance().getTable(tableSqlName);
      cache.put(tableSqlName.toLowerCase(), table);
    }
    return table;
  }

  public Sql parseSql(String sourceSql) {
    if (StringHelper.isBlank(sourceSql)) {
      throw new IllegalArgumentException("sourceSql must be not empty");
    }
    String beforeProcessedSql = beforeParseSql(sourceSql);

    // String unscapedSourceSql = StringHelper.unescapeXml(beforeProcessedSql);
    String namedSql = SqlParseHelper.convert2NamedParametersSql(beforeProcessedSql, ":", "");
    ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(namedSql);
    String executeSql =
        new BasicSqlFormatter().format(NamedParameterUtils.substituteNamedParameters(parsedSql));

    Sql sql = new Sql();
    sql.setSourceSql(sourceSql);
    sql.setExecuteSql(executeSql);
    log.debug("\n*******************************");
    log.debug("sourceSql  :" + sql.getSourceSql());
    log.debug("namedSql  :" + namedSql);
    log.debug("executeSql :" + sql.getExecuteSql());
    log.debug("*********************************");

    Connection conn = null;
    PreparedStatement ps = null;
    try {
      conn = DataSourceProvider.getNewConnection();
      conn.setAutoCommit(false);
      // if(DatabaseMetaDataUtils.isMysqlDataBase(conn.getMetaData())){
      // conn.setReadOnly(true);
      // }
      ps = conn.prepareStatement(SqlParseHelper.removeOrders(executeSql));

      SqlParametersParser sqlParametersParser = new SqlParametersParser();
      sqlParametersParser.execute(parsedSql, sql);

      ResultSetMetaData resultSetMetaData =
          executeSqlForResultSetMetaData(executeSql, ps, sqlParametersParser.allParams);
      sql.setColumns(new SelectColumnsParser().convert2Columns(sql, resultSetMetaData));
      sql.setParams(sqlParametersParser.params);

      return afterProcessedSql(sql);
    } catch (SQLException e) {
      throw new RuntimeException(
          "execute sql occer error,\nexecutedSql:" + SqlParseHelper.removeOrders(executeSql), e);
    } catch (Exception e) {
      throw new RuntimeException(
          "sql parse error,\nexecutedSql:" + SqlParseHelper.removeOrders(executeSql), e);
    } finally {
      try {
        DBHelper.rollback(conn);
      } finally {
        DBHelper.close(conn, ps, null);
      }
    }
  }

  protected Sql afterProcessedSql(Sql sql) {
    return sql;
  }

  protected String beforeParseSql(String sourceSql) {
    return sourceSql;
  }

  private ResultSetMetaData executeSqlForResultSetMetaData(String sql, PreparedStatement ps,
      List<SqlParameter> params) throws SQLException {
    //
    // SqlParseHelper.setRandomParamsValueForPreparedStatement(SqlParseHelper.removeOrders(executeSql),
    // ps);
    StatementCreatorUtils.setRandomParamsValueForPreparedStatement(sql, ps, params);

    try {
      ps.setMaxRows(3);
      ps.setFetchSize(3);
      ps.setQueryTimeout(20);
      ResultSet rs = null;
      if (ps.execute()) {
        rs = ps.getResultSet();
        return rs.getMetaData();
      }
      return null;
    } catch (SQLException e) {
      if (isDataIntegrityViolationException(e)) {
        log.warn("ignore executeSqlForResultSetMetaData() SQLException,errorCode:"
            + e.getErrorCode() + " sqlState:" + e.getSQLState() + " message:" + e.getMessage()
            + "\n executedSql:" + sql);
        return null;
      }
      String message = "errorCode:" + e.getErrorCode() + " SQLState:" + e.getSQLState()
          + " errorCodeTranslatorDataBaaseName:" + getErrorCodeTranslatorDataBaaseName() + " "
          + e.getMessage();
      throw new SQLException(message, e.getSQLState(), e.getErrorCode());
    }
  }

  private String getErrorCodeTranslatorDataBaaseName() {
    SQLErrorCodeSQLExceptionTranslator transaltor = SQLErrorCodeSQLExceptionTranslator
        .getSQLErrorCodeSQLExceptionTranslator(DataSourceProvider.getDataSource());
    if (transaltor.getSqlErrorCodes() == null) {
      return "null";
    }
    return Arrays.toString(transaltor.getSqlErrorCodes().getDatabaseProductNames());
  }

  /** 判断是否是外键,完整性约束等异常 引发的异常 */
  protected boolean isDataIntegrityViolationException(SQLException sqlEx) {
    SQLErrorCodeSQLExceptionTranslator transaltor = SQLErrorCodeSQLExceptionTranslator
        .getSQLErrorCodeSQLExceptionTranslator(DataSourceProvider.getDataSource());
    return transaltor.isDataIntegrityViolation(sqlEx);
  }
}
