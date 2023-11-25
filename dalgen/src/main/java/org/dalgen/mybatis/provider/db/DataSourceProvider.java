package org.dalgen.mybatis.provider.db;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.dalgen.mybatis.generator.GeneratorConstants;
import org.dalgen.mybatis.generator.GeneratorProperties;
import org.dalgen.mybatis.util.StringHelper;

import lombok.extern.slf4j.Slf4j;

/**
 * 用于提供生成器的数据源
 *
 * @author badqiu
 */
@Slf4j
public class DataSourceProvider {
  private static Connection connection;
  private static DataSource dataSource;

  public static synchronized Connection getNewConnection() {
    try {
      return getDataSource().getConnection();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static synchronized Connection getConnection() {
    try {
      if (connection == null || connection.isClosed()) {
        connection = getDataSource().getConnection();
      }
      return connection;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static synchronized DataSource getDataSource() {
    if (dataSource == null) {
      dataSource = lookupJndiDataSource(
          GeneratorProperties.getProperty(GeneratorConstants.DATA_SOURCE_JNDI_NAME));
      if (dataSource == null) {
        dataSource = new DriverManagerDataSource();
      }
    }
    return dataSource;
  }

  public static void setDataSource(DataSource dataSource) {
    DataSourceProvider.dataSource = dataSource;
  }

  private static DataSource lookupJndiDataSource(String name) {
    if (StringHelper.isBlank(name)) {
      return null;
    }

    try {
      Context context = new InitialContext();
      return (DataSource) context.lookup(name);
    } catch (NamingException e) {
      log.warn("lookup generator dataSource fail by name:" + name + " cause:" + e.toString()
          + ",retry by jdbc_url again");
      return null;
    }
  }
}
