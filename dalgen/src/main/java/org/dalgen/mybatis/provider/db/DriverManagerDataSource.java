package org.dalgen.mybatis.provider.db;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.dalgen.mybatis.generator.GeneratorConstants;
import org.dalgen.mybatis.generator.GeneratorProperties;

public class DriverManagerDataSource implements DataSource {

  public DriverManagerDataSource() {}

  private static void loadJdbcDriver(String driverClass) {
    try {
      if (driverClass == null || "".equals(driverClass.trim())) {
        throw new IllegalArgumentException("jdbc 'driverClass' must not be empty");
      }
      Class.forName(driverClass.trim());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("not found jdbc driver class:[" + driverClass + "]", e);
    }
  }

  @Override
  public Connection getConnection() throws SQLException {
    loadJdbcDriver(getDriverClass());
    return DriverManager.getConnection(getUrl(), getUsername(), getPassword());
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    loadJdbcDriver(getDriverClass());
    return DriverManager.getConnection(getUrl(), username, password);
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    throw new UnsupportedOperationException("getLogWriter");
  }

  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {
    throw new UnsupportedOperationException("setLogWriter");
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    throw new UnsupportedOperationException("getLoginTimeout");
  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {
    throw new UnsupportedOperationException("setLoginTimeout");
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return null;
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    if (iface == null) {
      throw new IllegalArgumentException("Interface argument must not be null");
    }
    if (!DataSource.class.equals(iface)) {
      throw new SQLException("DataSource of type [" + getClass().getName()
          + "] can only be unwrapped as [javax.sql.DataSource], not as [" + iface.getName());
    }
    return (T) this;
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return DataSource.class.equals(iface);
  }

  private String getUrl() {
    return GeneratorProperties.getRequiredProperty(GeneratorConstants.JDBC_URL);
  }

  private String getUsername() {
    return GeneratorProperties.getRequiredProperty(GeneratorConstants.JDBC_USERNAME);
  }

  private String getPassword() {
    return GeneratorProperties.getProperty(GeneratorConstants.JDBC_PASSWORD);
  }

  private String getDriverClass() {
    return GeneratorProperties.getRequiredProperty(GeneratorConstants.JDBC_DRIVER);
  }

  @Override
  public String toString() {
    return "DataSource: " + "url=" + getUrl() + " username=" + getUsername();
  }
}
