package org.dalgen.mybatis.provider.db.table;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.dalgen.mybatis.util.DBHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DatabaseMetaDataUtils {
  public static boolean isOracleDataBase(DatabaseMetaData metadata) {
    try {
      boolean ret = false;
      ret = (metadata.getDatabaseProductName().toLowerCase().indexOf("oracle") != -1);
      return ret;
    } catch (SQLException s) {
      return false;
      // throw new RuntimeException(s);
    }
  }

  public static boolean isHsqlDataBase(DatabaseMetaData metadata) {
    try {
      boolean ret = false;
      ret = (metadata.getDatabaseProductName().toLowerCase().indexOf("hsql") != -1);
      return ret;
    } catch (SQLException s) {
      return false;
      // throw new RuntimeException(s);
    }
  }

  public static boolean isMysqlDataBase(DatabaseMetaData metadata) {
    try {
      boolean ret = false;
      ret = (metadata.getDatabaseProductName().toLowerCase().indexOf("mysql") != -1);
      return ret;
    } catch (SQLException s) {
      return false;
      // throw new RuntimeException(s);
    }
  }

  public static DatabaseMetaData getMetaData(Connection connection) {
    try {
      return connection.getMetaData();
    } catch (SQLException e) {
      throw new RuntimeException("cannot get DatabaseMetaData", e);
    }
  }

  public static String getDatabaseStructureInfo(DatabaseMetaData metadata, String schema,
      String catalog) {
    ResultSet schemaRs = null;
    ResultSet catalogRs = null;
    String nl = System.getProperty("line.separator");
    StringBuffer sb = new StringBuffer(nl);
    // Let's give the user some feedback. The exception
    // is probably related to incorrect schema configuration.
    sb.append("Configured schema:").append(schema).append(nl);
    sb.append("Configured catalog:").append(catalog).append(nl);

    try {
      schemaRs = metadata.getSchemas();
      sb.append("Available schemas:").append(nl);
      while (schemaRs.next()) {
        sb.append("  ").append(schemaRs.getString("TABLE_SCHEM")).append(nl);
      }
    } catch (SQLException e2) {
      log.warn("Couldn't get schemas", e2);
      sb.append("  ?? Couldn't get schemas ??").append(nl);
    } finally {
      DBHelper.close(schemaRs);
    }

    try {
      catalogRs = metadata.getCatalogs();
      sb.append("Available catalogs:").append(nl);
      while (catalogRs.next()) {
        sb.append("  ").append(catalogRs.getString("TABLE_CAT")).append(nl);
      }
    } catch (SQLException e2) {
      log.warn("Couldn't get catalogs", e2);
      sb.append("  ?? Couldn't get catalogs ??").append(nl);
    } finally {
      DBHelper.close(catalogRs);
    }
    return sb.toString();
  }
}
