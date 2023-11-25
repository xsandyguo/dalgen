package org.dalgen.mybatis.ext.tableconfig;

import java.util.ArrayList;
import java.util.List;

import org.dalgen.mybatis.provider.db.sql.Sql;
import org.dalgen.mybatis.provider.db.table.Column;
import org.dalgen.mybatis.provider.db.table.Table;
import org.dalgen.mybatis.provider.db.table.TableFactory;
import org.dalgen.mybatis.util.StringHelper;

public class TableConfig {
  public String                sqlName;
  public String                sequence;
  public String                dummyPk;
  public String                remarks;

  public String                subPackage;
  public String                _package;
  public boolean               autoSwitchDataSrc;
  public String                className;

  public List<ColumnConfig>    columns     = new ArrayList<ColumnConfig>();
  public List<OperationConfig> operations  = new ArrayList<OperationConfig>();
  public List<ResultMapConfig> resultMaps  = new ArrayList<ResultMapConfig>();

  // for support
  // <sql id="columns"><![CDATA[ ]]></sql id="columns">
  // <include refid="columns"/>
  private List<SqlConfig>      includeSqls = new ArrayList<SqlConfig>();
  private Table                table       = null;
  private List<Sql>            sqls;

  public TableConfig() {}

  public static List<Sql> toSqls(TableConfig table) {
    return new Convert2SqlsProecssor().toSqls(table);
  }

  public List<ResultMapConfig> getResultMaps() {
    return resultMaps;
  }

  public void setResultMaps(List<ResultMapConfig> resultMaps) {
    this.resultMaps = resultMaps;
  }

  public String getClassName() {
    if (StringHelper.isNotBlank(className)) {
      return className;
    }
    if (StringHelper.isBlank(sqlName)) {
      return null;
    }
    return StringHelper.toJavaClassName(sqlName);
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public Column getPkColumn() throws Exception {
    if (StringHelper.isBlank(dummyPk)) {
      return getTable().getPkColumn();
    } else {
      return getTable().getColumnByName(dummyPk);
    }
  }

  public String getPackage() {
    if (StringHelper.isBlank(subPackage)) {
      return _package;
    } else {
      return _package + "." + subPackage;
    }
  }

  public void setPackage(String pkg) {
    this._package = pkg;
  }

  public Table getTable() throws Exception {
    if (table != null) {
      return table;
    }
    table = TableFactory.getInstance().getTable(getSqlName());
    return customTable(table);
  }

  public Table customTable(Table table) {
    if (!table.getSqlName().equalsIgnoreCase(getSqlName())) {
      throw new RuntimeException(
          "cannot custom table properties,sqlName not equals. tableConfig.sqlName:" + getSqlName()
              + " table.sqlName:" + table.getSqlName());
    }
    if (columns != null) {
      for (ColumnConfig c : columns) {
        Column tableColumn = table.getColumnByName(c.getName());
        if (tableColumn != null) {
          tableColumn.setJavaType(c.getJavatype()); // FIXME 只能自定义javaType
        }
      }
    }
    if (StringHelper.isNotBlank(getDummyPk())) {
      Column c = table.getColumnBySqlName(getDummyPk());
      if (c != null) {
        c.setPk(true);
      }
    }
    table.setClassName(getClassName());
    if (StringHelper.isNotBlank(remarks)) {
      table.setTableAlias(remarks);
    }
    return table;
  }

  public String getSqlName() {
    return sqlName;
  }

  public void setSqlName(String sqlname) {
    this.sqlName = sqlname;
  }

  public String getSequence() {
    return sequence;
  }

  public void setSequence(String sequence) {
    this.sequence = sequence;
  }

  public List<ColumnConfig> getColumns() {
    if (columns == null) {
      columns = new ArrayList();
    }
    return columns;
  }

  public void setColumns(List<ColumnConfig> column) {
    this.columns = column;
  }

  public List<SqlConfig> getIncludeSqls() {
    return includeSqls;
  }

  public void setIncludeSqls(List<SqlConfig> includeSqls) {
    this.includeSqls = includeSqls;
  }

  public void addSqlConfig(SqlConfig c) {
    this.includeSqls.add(c);
    c.setTableConfig(this);
  }

  public List<OperationConfig> getOperations() {
    return operations;
  }

  public void setOperations(List<OperationConfig> operations) {
    this.operations = operations;
  }

  public OperationConfig findOperation(String operationName) {
    OperationConfig operation = null;
    for (OperationConfig item : getOperations()) {
      if (item.getName().equals(operationName)) {
        return item;
      }
    }
    return null;
  }

  public String getDummyPk() {
    return dummyPk;
  }

  public void setDummyPk(String dummypk) {
    this.dummyPk = dummypk;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

  public String getSubPackage() {
    return subPackage;
  }

  public void setSubPackage(String subpackage) {
    this.subPackage = subpackage;
  }

  public boolean isAutoSwitchDataSrc() {
    return autoSwitchDataSrc;
  }

  public void setAutoSwitchDataSrc(boolean autoswitchdatasrc) {
    this.autoSwitchDataSrc = autoswitchdatasrc;
  }

  public void setDoName(String doname) {
    this.className = doname;
  }

  public String getBasepackage() {
    return getPackage();
  }

  @Override
  public String toString() {
    return "sqlname:" + sqlName;
  }

  public List<Sql> getSqls() {
    if (sqls == null) {
      sqls = toSqls(this);
    }
    return sqls;
  }

  public OperationConfig getOperation(String name) {
    for (OperationConfig op : operations) {
      if (op.getName().equals(name)) {
        return op;
      }
    }
    return null;
  }
}
