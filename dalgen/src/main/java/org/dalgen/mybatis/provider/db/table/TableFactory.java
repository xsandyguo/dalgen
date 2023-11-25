package org.dalgen.mybatis.provider.db.table;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.dalgen.mybatis.generator.GeneratorConstants;
import org.dalgen.mybatis.generator.GeneratorProperties;
import org.dalgen.mybatis.provider.db.DataSourceProvider;
import org.dalgen.mybatis.util.DBHelper;
import org.dalgen.mybatis.util.StringHelper;

/**
 * 根据数据库表的元数据(metadata)创建Table对象
 *
 * <pre>
 * getTable(sqlName) : 根据数据库表名,得到table对象
 * getAllTable() : 搜索数据库的所有表,并得到table对象列表
 * </pre>
 *
 * @author badqiu
 * @email badqiu(a)gmail.com
 */
public class TableFactory {

  private static TableFactory        instance              = null;

  private String                     schema;
  private String                     catalog;
  private List<TableFactoryListener> tableFactoryListeners = new ArrayList<TableFactoryListener>();

  private TableFactory(String schema, String catalog) {
    this.schema = schema;
    this.catalog = catalog;
  }

  public static synchronized TableFactory getInstance() {
    if (instance == null) {
      instance =
          new TableFactory(GeneratorProperties.getNullIfBlank(GeneratorConstants.JDBC_SCHEMA),
              GeneratorProperties.getNullIfBlank(GeneratorConstants.JDBC_CATALOG));
    }
    return instance;
  }

  public List<TableFactoryListener> getTableFactoryListeners() {
    return tableFactoryListeners;
  }

  public void setTableFactoryListeners(List<TableFactoryListener> tableFactoryListeners) {
    this.tableFactoryListeners = tableFactoryListeners;
  }

  public boolean addTableFactoryListener(TableFactoryListener o) {
    return tableFactoryListeners.add(o);
  }

  public void clearTableFactoryListener() {
    tableFactoryListeners.clear();
  }

  public boolean removeTableFactoryListener(TableFactoryListener o) {
    return tableFactoryListeners.remove(o);
  }

  public String getCatalog() {
    return catalog;
  }

  public String getSchema() {
    return schema;
  }

  public List getAllTables() {
    Connection conn = DataSourceProvider.getConnection();
    try {
      List<Table> tables = new TableCreateProcessor(conn, getSchema(), getCatalog()).getAllTables();
      for (Table t : tables) {
        dispatchOnTableCreatedEvent(t);
      }
      return tables;
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      DBHelper.close(conn);
    }
  }

  private void dispatchOnTableCreatedEvent(Table t) {
    for (TableFactoryListener listener : tableFactoryListeners) {
      listener.onTableCreated(t);
    }
  }

  public Table getTable(String tableName) {
    return getTable(getSchema(), tableName);
  }

  private Table getTable(String schema, String tableName) {
    return getTable(getCatalog(), schema, tableName);
  }

  private Table getTable(String catalog, String schema, String tableName) {
    Table t = null;
    try {
      t = _getTable(catalog, schema, tableName);
      if (t == null && !tableName.equals(tableName.toUpperCase())) {
        t = _getTable(catalog, schema, tableName.toUpperCase());
      }
      if (t == null && !tableName.equals(tableName.toLowerCase())) {
        t = _getTable(catalog, schema, tableName.toLowerCase());
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    if (t == null) {
      Connection conn = DataSourceProvider.getConnection();
      try {
        throw new NotFoundTableException("not found table with give name:" + tableName
            + (DatabaseMetaDataUtils.isOracleDataBase(DatabaseMetaDataUtils.getMetaData(conn))
                ? " \n databaseStructureInfo:" + DatabaseMetaDataUtils.getDatabaseStructureInfo(
                    DatabaseMetaDataUtils.getMetaData(conn), schema, catalog)
                : "")
            + "\n current " + DataSourceProvider.getDataSource() + " current schema:" + getSchema()
            + " current catalog:" + getCatalog());
      } finally {
        DBHelper.close(conn);
      }
    }
    dispatchOnTableCreatedEvent(t);
    return t;
  }

  private Table _getTable(String catalog, String schema, String tableName) throws SQLException {
    if (tableName == null || tableName.trim().length() == 0) {
      throw new IllegalArgumentException("tableName must be not empty");
    }
    catalog = StringHelper.defaultIfEmpty(catalog, null);
    schema = StringHelper.defaultIfEmpty(schema, null);

    Connection conn = DataSourceProvider.getConnection();
    DatabaseMetaData dbMetaData = conn.getMetaData();
    ResultSet rs = dbMetaData.getTables(catalog, schema, tableName, null);
    try {
      while (rs.next()) {
        Table table = new TableCreateProcessor(conn, getSchema(), getCatalog()).createTable(rs);
        return table;
      }
    } finally {
      DBHelper.close(conn, rs);
    }
    return null;
  }
}
