package org.dalgen.mybatis.provider.db.table;

import java.sql.*;
import java.util.*;

import org.dalgen.mybatis.util.BeanHelper;
import org.dalgen.mybatis.util.DBHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TableCreateProcessor {
  private Connection connection;
  private String     catalog;
  private String     schema;

  public TableCreateProcessor(Connection connection, String schema, String catalog) {
    super();
    this.connection = connection;
    this.schema = schema;
    this.catalog = catalog;
  }

  public String getCatalog() {
    return catalog;
  }

  public String getSchema() {
    return schema;
  }

  public Table createTable(ResultSet rs) throws SQLException {
    long start = System.currentTimeMillis();
    String tableName = null;
    try {
      ResultSetMetaData rsMetaData = rs.getMetaData();
      String schemaName = rs.getString("TABLE_SCHEM") == null ? "" : rs.getString("TABLE_SCHEM");
      tableName = rs.getString("TABLE_NAME");
      String tableType = rs.getString("TABLE_TYPE");
      String remarks = rs.getString("REMARKS");
      if (remarks == null && DatabaseMetaDataUtils.isOracleDataBase(connection.getMetaData())) {
        remarks = getOracleTableComments(tableName);
      }

      Table table = new Table();
      table.setSchema(schema);
      table.setCatalog(catalog);
      table.setSqlName(tableName);
      table.setRemarks(remarks);

      if ("SYNONYM".equals(tableType)
          && DatabaseMetaDataUtils.isOracleDataBase(connection.getMetaData())) {
        String[] ownerAndTableName = getSynonymOwnerAndTableName(tableName);
        table.setOwnerSynonymName(ownerAndTableName[0]);
        table.setTableSynonymName(ownerAndTableName[1]);
      }

      retriveTableColumns(table);
      table.initExportedKeys(connection.getMetaData());
      table.initImportedKeys(connection.getMetaData());
      BeanHelper.copyProperties(table,
          TableOverrideValuesProvider.getTableConfigValues(table.getSqlName()));
      return table;
    } catch (SQLException e) {
      throw new RuntimeException("create table object error,tableName:" + tableName, e);
    } finally {
      log.info(
          "createTable() cost:" + (System.currentTimeMillis() - start) + " tableName:" + tableName);
    }
  }

  public List<Table> getAllTables() throws SQLException {
    DatabaseMetaData dbMetaData = connection.getMetaData();
    ResultSet rs = dbMetaData.getTables(getCatalog(), getSchema(), null, null);
    try {
      List<Table> tables = new ArrayList<Table>();
      while (rs.next()) {
        tables.add(createTable(rs));
      }
      return tables;
    } finally {
      DBHelper.close(rs);
    }
  }

  private String[] getSynonymOwnerAndTableName(String synonymName) {
    PreparedStatement ps = null;
    ResultSet rs = null;
    String[] ret = new String[2];
    try {
      ps = connection.prepareStatement(
          "select table_owner,table_name from sys.all_synonyms where synonym_name=? and owner=?");
      ps.setString(1, synonymName);
      ps.setString(2, getSchema());
      rs = ps.executeQuery();
      if (rs.next()) {
        ret[0] = rs.getString(1);
        ret[1] = rs.getString(2);
      } else {
        String databaseStructure =
            DatabaseMetaDataUtils.getDatabaseStructureInfo(getMetaData(), schema, catalog);
        throw new RuntimeException(
            "Wow! Synonym " + synonymName + " not found. How can it happen? " + databaseStructure);
      }
    } catch (SQLException e) {
      String databaseStructure =
          DatabaseMetaDataUtils.getDatabaseStructureInfo(getMetaData(), schema, catalog);
      log.error(e.getMessage(), e);
      throw new RuntimeException("Exception in getting synonym owner " + databaseStructure);
    } finally {
      DBHelper.close(null, ps, rs);
    }
    return ret;
  }

  private DatabaseMetaData getMetaData() {
    return DatabaseMetaDataUtils.getMetaData(connection);
  }

  private void retriveTableColumns(Table table) throws SQLException {
    log.trace("-------setColumns(" + table.getSqlName() + ")");

    List primaryKeys = getTablePrimaryKeys(table);
    table.setPrimaryKeyColumns(primaryKeys);

    // get the indices and unique columns
    List indices = new LinkedList();
    // maps index names to a list of columns in the index
    Map uniqueIndices = new HashMap();
    // maps column names to the index name.
    Map uniqueColumns = new HashMap();
    ResultSet indexRs = null;

    try {

      if (table.getOwnerSynonymName() != null) {
        indexRs = getMetaData().getIndexInfo(getCatalog(), table.getOwnerSynonymName(),
            table.getTableSynonymName(), false, true);
      } else {
        indexRs =
            getMetaData().getIndexInfo(getCatalog(), getSchema(), table.getSqlName(), false, true);
      }
      while (indexRs.next()) {
        String columnName = indexRs.getString("COLUMN_NAME");
        if (columnName != null) {
          log.trace("index:" + columnName);
          indices.add(columnName);
        }

        // now look for unique columns
        String indexName = indexRs.getString("INDEX_NAME");
        boolean nonUnique = indexRs.getBoolean("NON_UNIQUE");

        if (!nonUnique && columnName != null && indexName != null) {
          List l = (List) uniqueColumns.get(indexName);
          if (l == null) {
            l = new ArrayList();
            uniqueColumns.put(indexName, l);
          }
          l.add(columnName);
          uniqueIndices.put(columnName, indexName);
          log.trace("unique:" + columnName + " (" + indexName + ")");
        }
      }
    } catch (Throwable t) {
      // Bug #604761 Oracle getIndexInfo() needs major grants
      // http://sourceforge.net/tracker/index.php?func=detail&aid=604761&group_id=36044&atid=415990
    } finally {
      DBHelper.close(indexRs);
    }

    List columns = getTableColumns(table, primaryKeys, indices, uniqueIndices, uniqueColumns);

    for (Iterator i = columns.iterator(); i.hasNext();) {
      Column column = (Column) i.next();
      table.addColumn(column);
    }

    // In case none of the columns were primary keys, issue a warning.
    if (primaryKeys.size() == 0) {
      log.warn("WARNING: The JDBC driver didn't report any primary key columns in "
          + table.getSqlName());
    }
  }

  private List getTableColumns(Table table, List primaryKeys, List indices, Map uniqueIndices,
      Map uniqueColumns) throws SQLException {
    // get the columns
    List columns = new LinkedList();
    ResultSet columnRs = getColumnsResultSet(table);
    try {
      while (columnRs.next()) {
        int sqlType = columnRs.getInt("DATA_TYPE");
        String sqlTypeName = columnRs.getString("TYPE_NAME");
        String columnName = columnRs.getString("COLUMN_NAME");
        String columnDefaultValue = columnRs.getString("COLUMN_DEF");

        String remarks = columnRs.getString("REMARKS");
        if (remarks == null && DatabaseMetaDataUtils.isOracleDataBase(connection.getMetaData())) {
          remarks = getOracleColumnComments(table.getSqlName(), columnName);
        }

        // if columnNoNulls or columnNullableUnknown assume "not nullable"
        boolean isNullable = (DatabaseMetaData.columnNullable == columnRs.getInt("NULLABLE"));
        int size = columnRs.getInt("COLUMN_SIZE");
        int decimalDigits = columnRs.getInt("DECIMAL_DIGITS");

        boolean isPk = primaryKeys.contains(columnName);
        boolean isIndexed = indices.contains(columnName);
        String uniqueIndex = (String) uniqueIndices.get(columnName);
        List columnsInUniqueIndex = null;
        if (uniqueIndex != null) {
          columnsInUniqueIndex = (List) uniqueColumns.get(uniqueIndex);
        }

        boolean isUnique = columnsInUniqueIndex != null && columnsInUniqueIndex.size() == 1;
        if (isUnique) {
          log.trace("unique column:" + columnName);
        }
        Column column = new Column(table, sqlType, sqlTypeName, columnName, size, decimalDigits,
            isPk, isNullable, isIndexed, isUnique, columnDefaultValue, remarks);
        BeanHelper.copyProperties(column,
            TableOverrideValuesProvider.getColumnConfigValues(table, column));
        columns.add(column);
      }
    } finally {
      DBHelper.close(columnRs);
    }
    return columns;
  }

  private ResultSet getColumnsResultSet(Table table) throws SQLException {
    ResultSet columnRs = null;
    if (table.getOwnerSynonymName() != null) {
      columnRs = getMetaData().getColumns(getCatalog(), table.getOwnerSynonymName(),
          table.getTableSynonymName(), null);
    } else {
      columnRs = getMetaData().getColumns(getCatalog(), getSchema(), table.getSqlName(), null);
    }
    return columnRs;
  }

  private List<String> getTablePrimaryKeys(Table table) throws SQLException {
    // get the primary keys
    List primaryKeys = new LinkedList();
    ResultSet primaryKeyRs = null;
    try {
      if (table.getOwnerSynonymName() != null) {
        primaryKeyRs = getMetaData().getPrimaryKeys(getCatalog(), table.getOwnerSynonymName(),
            table.getTableSynonymName());
      } else {
        primaryKeyRs = getMetaData().getPrimaryKeys(getCatalog(), getSchema(), table.getSqlName());
      }
      while (primaryKeyRs.next()) {
        String columnName = primaryKeyRs.getString("COLUMN_NAME");
        log.trace("primary key:" + columnName);
        primaryKeys.add(columnName);
      }
    } finally {
      DBHelper.close(primaryKeyRs);
    }
    return primaryKeys;
  }

  // FIXME 如果是oracle同义词:Synonym, 需要根据 OwnerSynonymName及TableSynonymName 才能查找回oracle注释
  private String getOracleTableComments(String table) {
    String sql = "SELECT comments FROM user_tab_comments WHERE table_name='" + table + "'";
    return ExecuteSqlHelper.queryForString(connection, sql);
  }

  private String getOracleColumnComments(String table, String column) {
    String sql = "SELECT comments FROM user_col_comments WHERE table_name='" + table
        + "' AND column_name = '" + column + "'";
    return ExecuteSqlHelper.queryForString(connection, sql);
  }
}
