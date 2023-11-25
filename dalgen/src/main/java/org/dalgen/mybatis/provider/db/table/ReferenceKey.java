package org.dalgen.mybatis.provider.db.table;

import org.dalgen.mybatis.util.StringHelper;

/**
 * 外键引用使用的值对象
 *
 * @author badqiu
 */
public class ReferenceKey implements java.io.Serializable {
  public String schemaName;
  public String tableName;
  public String columnSqlName;

  public ReferenceKey(String schemaName, String tableName, String columnSqlName) {
    this.schemaName = StringHelper.defaultIfEmpty(schemaName, null);
    this.tableName = tableName;
    this.columnSqlName = columnSqlName;
  }

  public static String toString(ReferenceKey k) {
    if (k == null)
      return null;
    return k.toString();
  }

  /**
   * 解析foreignKey字符串,格式: fk_table_name(fk_column) 或者 schema_name.fk_table_name(fk_column)
   *
   * @param foreignKey
   * @return
   */
  public static ReferenceKey fromString(String foreignKey) {
    if (StringHelper.isBlank(foreignKey)) {
      return null;
    }
    if (!foreignKey.trim().matches(".*\\w+\\(.*\\)")) {
      throw new IllegalArgumentException(
          "Illegal foreignKey:[" + foreignKey + "] ,example value: fk_table_name(fk_column) ");
    }
    String schemaName = foreignKey.substring(0, Math.max(foreignKey.lastIndexOf("."), 0));
    String tableSqlName =
        foreignKey.substring(Math.max(foreignKey.lastIndexOf(".") + 1, 0), foreignKey.indexOf("("));
    String columnSqlName =
        foreignKey.substring(foreignKey.indexOf("(") + 1, foreignKey.indexOf(")"));
    return new ReferenceKey(schemaName, tableSqlName, columnSqlName);
  }

  public String toString() {
    if (StringHelper.isBlank(schemaName)) {
      return tableName + "(" + columnSqlName + ")";
    } else {
      return schemaName + "." + tableName + "(" + columnSqlName + ")";
    }
  }
}
