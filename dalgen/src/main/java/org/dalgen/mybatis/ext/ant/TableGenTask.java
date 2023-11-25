package org.dalgen.mybatis.ext.ant;

import java.util.*;

import org.dalgen.mybatis.provider.db.table.Table;
import org.dalgen.mybatis.provider.db.table.TableFactory;
import org.dalgen.mybatis.util.BeanHelper;

public class TableGenTask extends BaseGeneratorTask {
  private String tableSqlName;

  @Override
  protected List<Map> getGeneratorContexts() {
    if ("*".equals(tableSqlName)) {
      List result = new ArrayList();
      List<Table> tables = TableFactory.getInstance().getAllTables();
      for (Table t : tables) {
        Map map = toMap(t);
        result.add(map);
      }
      return result;
    } else {
      Table table = TableFactory.getInstance().getTable(tableSqlName);
      if (table == null) {
        log("没有找到该表:" + tableSqlName);
        return null;
      }
      Map map = toMap(table);
      return Arrays.asList(map);
    }
  }

  private Map toMap(Table table) {
    Map map = new HashMap();
    map.putAll(BeanHelper.describe(table));
    map.put("table", table);
    return map;
  }

  public String getTableSqlName() {
    return tableSqlName;
  }

  public void setTableSqlName(String tableSqlName) {
    this.tableSqlName = tableSqlName;
  }
}
