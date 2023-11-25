package org.dalgen.mybatis.ext.tableconfig;

import java.util.*;

import org.dalgen.mybatis.generator.GeneratorProperties;
import org.dalgen.mybatis.provider.db.sql.Sql;
import org.dalgen.mybatis.provider.db.sql.SqlFactory;
import org.dalgen.mybatis.provider.db.sql.SqlParameter;
import org.dalgen.mybatis.provider.db.table.Column;
import org.dalgen.mybatis.provider.db.table.ColumnSet;
import org.dalgen.mybatis.provider.db.table.Table;
import org.dalgen.mybatis.sqlparse.SqlParseHelper;
import org.dalgen.mybatis.typemapping.JdbcType;
import org.dalgen.mybatis.util.StringHelper;

/** 用于将 OperationConfig.class 解析转换成 Sql.class对象 */
class Convert2SqlsProecssor {

  private static String getIbatisSql(OperationConfig op, Sql sql) {
    String ibatisNamedSql = sql.replaceWildcardWithColumnsSqlName(
        SqlParseHelper.convert2NamedParametersSql(op.getSql(), "#", "#")) + " "
        + StringHelper.defaultString(op.getAppend());
    String ibatisSql = processSqlForMoneyParam(ibatisNamedSql, sql.getParams());
    return ibatisSql;
  }

  private static LinkedHashSet<Column> processWithCustomColumns(List<Column> customColumns,
      LinkedHashSet<Column> columns) {
    ColumnSet columnSet = new ColumnSet(customColumns);
    for (Column c : columns) {
      Column custom = columnSet.getBySqlName(c.getSqlName());
      if (custom != null) {
        c.setJavaType(custom.getJavaType());
      }
    }
    return columns;
  }

  private static LinkedHashSet<SqlParameter> addExtraParams2SqlParams(List<ParamConfig> extraParams,
      Sql sql) {
    LinkedHashSet<SqlParameter> filterdExtraParameters = new LinkedHashSet<SqlParameter>();
    for (ParamConfig extraParam : extraParams) {
      SqlParameter param = sql.getParam(extraParam.getName());
      if (param == null) {
        SqlParameter extraparam = new SqlParameter();
        extraparam.setParameterClass(extraParam.getJavatype());
        if (StringHelper.isNotBlank(extraParam.getColumnAlias())) {
          extraparam.setColumnAlias(extraParam.getColumnAlias()); // FIXME extraparam alias
          // 现在的处理方式不好,应该不使用StringHelper.isNotBlank()判断
        }
        extraparam.setParamName(extraParam.getName());
        filterdExtraParameters.add(extraparam);
      } else {
        param.setParameterClass(extraParam.getJavatype());
        if (StringHelper.isNotBlank(extraParam.getColumnAlias())) {
          param.setColumnAlias(extraParam.getColumnAlias()); // FIXME extraparam alias
          // 现在的处理方式不好,应该不使用StringHelper.isNotBlank()判断
        }
      }
    }
    if (GeneratorProperties.getBoolean("generator.extraParams.append", true)) {
      LinkedHashSet result = new LinkedHashSet(sql.getParams());
      result.addAll(filterdExtraParameters);
      return result;
    } else {
      filterdExtraParameters.addAll(sql.getParams());
      return filterdExtraParameters;
    }
  }

  /** Money类的特殊转换，only for alipay */
  private static String processSqlForMoneyParam(String ibatisSql,
      LinkedHashSet<SqlParameter> params) {
    for (SqlParameter p : params) {
      if (p.getParameterClass().endsWith("Money")) {
        ibatisSql = StringHelper.replace(ibatisSql, "#" + p.getParamName() + "#",
            "#" + p.getParamName() + ".cent" + "#");
        ibatisSql = StringHelper.replace(ibatisSql, "#{" + p.getParamName() + "}",
            "#{" + p.getParamName() + ".cent" + "}");
      }
    }
    return ibatisSql;
  }

  private static Map<String, String> toMap(List<SqlConfig> sql) {
    Map map = new HashMap();
    for (SqlConfig s : sql) {
      map.put(s.id, s.sql);
    }
    return map;
  }

  private static List<Column> getCustomColumns(TableConfig table) throws Exception {
    List<Column> result = new ArrayList<Column>();
    Table t = table.getTable();
    for (ColumnConfig mc : table.getColumns()) {
      Column c = t.getColumnByName(mc.getName());
      if (c == null) {
        c = new Column(null, JdbcType.UNDEFINED.TYPE_CODE, "UNDEFINED", mc.getName(), -1, -1, false,
            false, false, false, "", mc.getColumnAlias());
      }
      c.setJavaType(mc.getJavatype());
      if (StringHelper.isNotBlank(mc.getColumnAlias())) {
        c.setColumnAlias(mc.getColumnAlias()); // FIXME custom column的 alias
        // 现在的处理方式不好,应该不使用StringHelper.isNotBlank()判断
      }
      result.add(c);
    }
    return result;
  }

  public List<Sql> toSqls(TableConfig table) {
    List<Sql> sqls = new ArrayList<Sql>();
    for (OperationConfig op : table.getOperations()) {
      sqls.add(processOperation(op, table));
    }
    return sqls;
  }

  public Sql toSql(TableConfig table, String operationName) {
    OperationConfig operation = table.findOperation(operationName);
    if (operation == null) {
      throw new IllegalArgumentException("not found operation with name:" + operationName);
    }
    return processOperation(operation, table);
  }

  private Sql processOperation(OperationConfig op, TableConfig table) {
    try {
      IbatisSqlMapConfigParser ibatisSqlMapConfigParser = new IbatisSqlMapConfigParser();
      String sqlString = ibatisSqlMapConfigParser.parse(op.getSql(), toMap(table.getIncludeSqls()));

      // 确认要删除本行?,因为与SqlFactory里面的代码重复
      String namedSql = SqlParseHelper.convert2NamedParametersSql(sqlString, ":", ""); // TODO


      Sql sql = new SqlFactory().parseSql(namedSql);
      sql.setSqlSegments(ibatisSqlMapConfigParser.getSqlSegments());

      LinkedHashSet<SqlParameter> finalParameters =
          addExtraParams2SqlParams(op.getExtraparams(), sql);
      sql.setParams(finalParameters);
      sql.setColumns(processWithCustomColumns(getCustomColumns(table), sql.getColumns()));

      String ibatisSql = getIbatisSql(op, sql);
      sql.setIbatisSql(ibatisSql);
      sql.setMybatisSql(sql.replaceWildcardWithColumnsSqlName(
          SqlParseHelper.convert2NamedParametersSql(op.getSql(), "#{", "}")) + " "
          + op.getAppend()); // FIXME 修正ibatis3的问题

      sql.setOperation(op.getName());
      sql.setParameterClass(op.getParameterClass());
      sql.setResultClass(op.getResultClass());
      sql.setRemarks(op.getRemarks());
      sql.setPaging(op.isPaging());
      sql.setSqlmap(op.getSqlmap());
      sql.setResultMap(op.getResultMap());

      // FIXME 增加insert append="nowait"至 CDATA ]]>结尾的前面

      if (StringHelper.isNotBlank(op.getMultiplicity())) {
        sql.setMultiplicity(op.getMultiplicity());
      }

      // FIXME 与dalgen的规则是否一致
      if (StringHelper.isNotBlank(op.getParamtype())) {
        sql.setParamType(op.getParamtype());
      } else if (StringHelper.isBlank(op.getParamtype())
          && (sql.isSelectSql() || sql.isDeleteSql())) {
        sql.setParamType(Sql.PARAMTYPE_PRIMITIVE);
      }

      sql.afterPropertiesSet();
      return afterProcessed(sql, op, table);
    } catch (Exception e) {
      throw new RuntimeException("parse sql error on table:" + table.getSqlName() + " operation:"
          + op.getName() + "() sql:" + op.getSql(), e);
    }
  }

  protected Sql afterProcessed(Sql sql, OperationConfig op, TableConfig table) {
    return sql;
  }
}
