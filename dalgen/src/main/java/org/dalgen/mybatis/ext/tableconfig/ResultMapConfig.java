package org.dalgen.mybatis.ext.tableconfig;

import java.util.ArrayList;
import java.util.List;

public class ResultMapConfig {
  private String             name;
  private List<ColumnConfig> columns = new ArrayList();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<ColumnConfig> getColumns() {
    return columns;
  }

  public void setColumns(List<ColumnConfig> columns) {
    this.columns = columns;
  }
}
