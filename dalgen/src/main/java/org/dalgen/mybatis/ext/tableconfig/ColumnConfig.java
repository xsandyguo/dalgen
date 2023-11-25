package org.dalgen.mybatis.ext.tableconfig;

import org.dalgen.mybatis.typemapping.JavaPrimitiveTypeMapping;
import org.dalgen.mybatis.util.BeanHelper;
import org.dalgen.mybatis.util.StringHelper;

public class ColumnConfig {
  private String name;
  private String javatype;
  private String columnAlias;
  private String nullValue;

  public String getName() {
    return name;
  }

  public void setName(String sqlname) {
    this.name = sqlname;
  }

  public String getJavatype() {
    return javatype;
  }

  public void setJavatype(String javatype) {
    this.javatype = javatype;
  }

  public String getColumnAlias() {
    return columnAlias;
  }

  public void setColumnAlias(String columnAlias) {
    this.columnAlias = columnAlias;
  }

  public String getNullValue() {
    if (StringHelper.isBlank(nullValue)) {
      return JavaPrimitiveTypeMapping.getDefaultValue(javatype);
    } else {
      return nullValue;
    }
  }

  public void setNullValue(String nullValue) {
    this.nullValue = nullValue;
  }

  public boolean isHasNullValue() {
    return JavaPrimitiveTypeMapping.getWrapperTypeOrNull(javatype) != null;
  }

  public String toString() {
    return BeanHelper.describe(this).toString();
  }
}
