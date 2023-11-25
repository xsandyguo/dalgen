package org.dalgen.mybatis.provider.db.table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.dalgen.mybatis.util.DBHelper;

public class ExecuteSqlHelper {

  public static String queryForString(Connection conn, String sql) {
    Statement s = null;
    ResultSet rs = null;
    try {
      s = conn.createStatement();
      rs = s.executeQuery(sql);
      if (rs.next()) {
        return rs.getString(1);
      }
      return null;
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    } finally {
      DBHelper.close(null, s, rs);
    }
  }
}
