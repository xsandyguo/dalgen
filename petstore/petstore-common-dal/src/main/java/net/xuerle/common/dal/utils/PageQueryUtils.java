/*
 * petstore.net Inc.
 * Copyright (c) 2020 - 2023 All Rights Reserved.
 * Powered By [dalgen]
 */

package net.petstore.common.dal.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.orm.ibatis.SqlMapClientTemplate;

/**
 * Ibatis的分页查询工具类. 将为分页查询提供附加的四个参数 基于0开始的: offset, limit 用于mysql分页查询 基于1开始的: startRow,endRow
 * 用于oracle分页查询
 *
 * @author badqiu
 * @version $Id: PageQueryUtils.java,v 0.1 2010-7-28 下午04:45:27 badqiu Exp $
 */
@SuppressWarnings("unchecked")
public class PageQueryUtils {
  /**
   * 封装ibatis的分页查询
   *
   * @param sqlMapClientTemplate
   * @param statementName
   * @param parameterObject
   * @return
   */
  public static PageList pageQuery(
      SqlMapClientTemplate sqlMapClientTemplate, String statementName, PageQuery parameterObject) {
    return pageQuery(
        sqlMapClientTemplate,
        statementName,
        statementName + ".count",
        parameterObject,
        parameterObject.getPage(),
        parameterObject.getPageSize());
  }

  /**
   * 封装ibatis的分页查询
   *
   * @return
   */
  public static PageList pageQuery(
      SqlMapClientTemplate sqlMapClientTemplate,
      String statementName,
      Object parameterObject,
      int page,
      int pageSize) {
    return pageQuery(
        sqlMapClientTemplate,
        statementName,
        statementName + ".count",
        parameterObject,
        page,
        pageSize);
  }

  /**
   * 封装ibatis的分页查询
   *
   * @return
   */
  public static PageList pageQuery(
      SqlMapClientTemplate sqlMapClientTemplate,
      String statementName,
      String countStatementName,
      Object parameterObject,
      int page,
      int pageSize) {

    Number totalCount =
        (Number) sqlMapClientTemplate.queryForObject(countStatementName, parameterObject);

    if (totalCount != null && totalCount.intValue() > 0) {
      Paginator paginator = new Paginator(page, pageSize, totalCount.intValue());

      Map<String, Integer> otherParams = new HashMap<>();
      otherParams.put("offset", paginator.getOffset());
      otherParams.put("limit", paginator.getLimit());
      otherParams.put("startRow", paginator.getStartRow());
      otherParams.put("endRow", paginator.getEndRow());

      List list =
          sqlMapClientTemplate.queryForList(
              statementName, new MapAndObject(otherParams, parameterObject));
      return new PageList(list, paginator);
    }
    return new PageList(new Paginator(0, pageSize, 0));
  }


  public static <T> PageList<T> pageQuery(SqlSession sqlSession, String statementName,
                                          Object parameterObject, int pageNum, int pageSize) {
    return pageQuery(sqlSession, statementName, statementName + "_count", parameterObject, pageNum,
            pageSize);
  }

  public static <T> PageList<T> pageQuery(SqlSession sqlSession, String statementName,
                                          PageQuery parameterObject) {
    return pageQuery(sqlSession, statementName, statementName + "_count", parameterObject,
            parameterObject.getPage(), parameterObject.getPageSize());
  }

  public static <T> PageList<T> pageQuery(SqlSession sqlSession, String statementName,
                                          String countStatementName, Object parameterObject, int page, int pageSize) {
    Number totalCount = sqlSession.selectOne(countStatementName, parameterObject);

    if (totalCount != null && totalCount.intValue() > 0) {
      Paginator paginator = new Paginator(page, pageSize, totalCount.intValue());

      Map<String, Integer> otherParams = new HashMap<>();
      otherParams.put("offset", paginator.getOffset());
      otherParams.put("limit", paginator.getLimit());
      otherParams.put("startRow", paginator.getStartRow());
      otherParams.put("endRow", paginator.getEndRow());

      List list =
              sqlSession.selectList(statementName, new MapAndObject(otherParams, parameterObject));
      return new PageList(list, paginator);
    }
    return new PageList(new Paginator(0, pageSize, 0));
  }
}
