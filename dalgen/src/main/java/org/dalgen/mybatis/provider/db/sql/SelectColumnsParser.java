package org.dalgen.mybatis.provider.db.sql;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.dalgen.mybatis.provider.db.table.Column;
import org.dalgen.mybatis.provider.db.table.NotFoundTableException;
import org.dalgen.mybatis.provider.db.table.Table;
import org.dalgen.mybatis.sqlparse.NameWithAlias;
import org.dalgen.mybatis.sqlparse.ResultSetMetaDataHolder;
import org.dalgen.mybatis.sqlparse.SqlParseHelper;
import org.dalgen.mybatis.util.BeanHelper;
import org.dalgen.mybatis.util.StringHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SelectColumnsParser {

  public LinkedHashSet<Column> convert2Columns(Sql sql, ResultSetMetaData metadata)
      throws SQLException, Exception {
    if (metadata == null)
      return new LinkedHashSet();
    LinkedHashSet<Column> columns = new LinkedHashSet();
    for (int i = 1; i <= metadata.getColumnCount(); i++) {
      Column c = convert2Column(sql, metadata, i);
      if (c == null)
        throw new IllegalStateException("column must be not null");
      columns.add(c);
    }
    return columns;
  }

  private Column convert2Column(Sql sql, ResultSetMetaData metadata, int i)
      throws SQLException, Exception {
    ResultSetMetaDataHolder m = new ResultSetMetaDataHolder(metadata, i);
    if (StringHelper.isNotBlank(m.getTableName())) {
      // FIXME 如果表有别名,将会找不到表,如 inner join user_info t1, tableName将为t1,应该转换为user_info
      Table table = foundTableByTableNameOrTableAlias(sql, m.getTableName());
      if (table == null) {
        return newColumn(null, m);
      }
      Column column = table.getColumnBySqlName(m.getColumnLabelOrName());
      if (column == null || column.getSqlType() != m.getColumnType()) {
        // 可以再尝试解析sql得到 column以解决 password as pwd找不到column问题
        column = newColumn(table, m);
        log.trace("not found column:" + m.getColumnLabelOrName() + " on table:" + table.getSqlName()
            + " " + BeanHelper.describe(column));
        // isInSameTable以此种判断为错误
      } else {
        log.trace("found column:" + m.getColumnLabelOrName() + " on table:" + table.getSqlName()
            + " " + BeanHelper.describe(column));
      }
      return column;
    } else {
      return newColumn(null, m);
    }
  }

  private Column newColumn(Table table, ResultSetMetaDataHolder m) {
    // Table table, int sqlType, String sqlTypeName,String sqlName, int size, int decimalDigits,
    // boolean isPk,boolean isNullable, boolean isIndexed, boolean isUnique,String
    // defaultValue,String remarks
    Column column =
        new Column(null, m.getColumnType(), m.getColumnTypeName(), m.getColumnLabelOrName(),
            m.getColumnDisplaySize(), m.getScale(), false, false, false, false, null, null);
    log.trace("not found on table by table emtpty:" + BeanHelper.describe(column));
    return column;
  }

  private Table foundTableByTableNameOrTableAlias(Sql sql, String tableNameId) throws Exception {
    try {
      return SqlFactory.getTableFromCache(tableNameId);
    } catch (NotFoundTableException e) {
      Set<NameWithAlias> tableNames = SqlParseHelper.getTableNamesByQuery(sql.getExecuteSql());
      for (NameWithAlias tableName : tableNames) {
        if (tableName.getAlias().equalsIgnoreCase(tableNameId)) {
          return SqlFactory.getTableFromCache(tableName.getName());
        }
      }
    }
    return null;
  }
}
