package org.dalgen.mybatis.ext.tableconfig;

import org.dalgen.mybatis.provider.db.sql.Sql;
import org.dalgen.mybatis.provider.db.sql.SqlSegment;

/** 代表被 include的一段sql */
public class SqlConfig {
  String              id;
  String              sql;
  private TableConfig tableConfig;

  @Override
  public String toString() {
    return String.format("<sql id='%s'>%s</sql>", id, sql);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSql() {
    return sql;
  }

  public void setSql(String sql) {
    this.sql = sql;
  }

  public TableConfig getTableConfig() {
    return tableConfig;
  }

  public void setTableConfig(TableConfig tableConfig) {
    this.tableConfig = tableConfig;
  }

  public SqlSegment getSqlSegment() {
    if (tableConfig == null)
      throw new IllegalArgumentException("tableConfig must be not null");
    for (Sql sql : tableConfig.getSqls()) {
      if (sql.getSqlSegment(id) != null) {
        return sql.getSqlSegment(id);
      }
    }
    return null;
  }
}
