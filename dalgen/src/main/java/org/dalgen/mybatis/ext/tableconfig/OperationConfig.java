package org.dalgen.mybatis.ext.tableconfig;

import java.util.ArrayList;
import java.util.List;

import org.dalgen.mybatis.provider.db.sql.Sql;
import org.dalgen.mybatis.util.BeanHelper;

public class OperationConfig {
  public List<ParamConfig> extraparams         = new ArrayList<ParamConfig>();
  public String            name;
  public String            resultClass;
  public String            resultMap;
  public String            parameterClass;
  public String            remarks;
  public String            multiplicity;
  public String            paramtype;
  public String            sql;
  public String            sqlmap;
  public Sql               parsedSql;
  public boolean           paging              = false;

  public String            append              = "";                          // append为无用配置,only
                                                                              // for alipay的兼容性
  public String            appendXmlAttributes = "";                          // TODO 还没有实现

  public List<ParamConfig> getExtraparams() {
    return extraparams;
  }

  public void setExtraparams(List<ParamConfig> extraparams) {
    this.extraparams = extraparams;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getResultClass() {
    return resultClass;
  }

  public void setResultClass(String resultClass) {
    this.resultClass = resultClass;
  }

  public String getParameterClass() {
    return parameterClass;
  }

  public void setParameterClass(String parameterClass) {
    this.parameterClass = parameterClass;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

  public String getMultiplicity() {
    return multiplicity;
  }

  public void setMultiplicity(String multiplicity) {
    this.multiplicity = multiplicity;
  }

  public String getSql() {
    return sql;
  }

  public void setSql(String sql) {
    this.sql = sql;
  }

  public String getSqlmap() {
    return sqlmap;
  }

  public void setSqlmap(String sqlmap) {
    this.sqlmap = sqlmap;
  }

  public String getParamtype() {
    return paramtype;
  }

  public void setParamtype(String paramtype) {
    this.paramtype = paramtype;
  }

  public boolean isPaging() {
    return paging;
  }

  public void setPaging(boolean paging) {
    this.paging = paging;
  }

  public String getResultMap() {
    return resultMap;
  }

  public void setResultMap(String resultMap) {
    this.resultMap = resultMap;
  }

  public String getAppend() {
    return append;
  }

  public void setAppend(String append) {
    this.append = append;
  }

  public String toString() {
    return BeanHelper.describe(this).toString();
  }
}
