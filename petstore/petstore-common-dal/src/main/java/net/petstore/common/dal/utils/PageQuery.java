/*
 * petstore.net Inc.
 * Copyright (c) 2020 - 2023 All Rights Reserved.
 * Powered By [dalgen]
 */

package net.petstore.common.dal.utils;
/**
 * 分页查询对象
 *
 * @author badqiu
 * @author hunhun
 * @version $Id : PageQuery.java,v 0.1 2010-11-29 下午05:34:12 badqiu Exp $
 */
public class PageQuery implements java.io.Serializable {
  private static final long serialVersionUID = -8000900575354501298L;

  /** The constant DEFAULT_PAGE_SIZE. */
  public static final int DEFAULT_PAGE_SIZE = 10;

  /** 页数 */
  private int page;

  /** 分页大小 */
  private int pageSize = DEFAULT_PAGE_SIZE;

  /** Instantiates a new Page query. */
  public PageQuery() {}

  /**
   * Instantiates a new Page query.
   *
   * @param pageSize the page size
   */
  public PageQuery(int pageSize) {
    this.pageSize = pageSize;
  }

  /**
   * Instantiates a new Page query.
   *
   * @param query the query
   */
  public PageQuery(PageQuery query) {
    this.page = query.page;
    this.pageSize = query.pageSize;
  }

  /**
   * Instantiates a new Page query.
   *
   * @param page the page
   * @param pageSize the page size
   */
  public PageQuery(int page, int pageSize) {
    this.page = page;
    this.pageSize = pageSize;
  }

  /**
   * Gets page.
   *
   * @return the page
   */
  public int getPage() {
    return page;
  }

  /**
   * Sets page.
   *
   * @param page the page
   */
  public void setPage(int page) {
    this.page = page;
  }

  /**
   * Gets page size.
   *
   * @return the page size
   */
  public int getPageSize() {
    return pageSize;
  }

  /**
   * Sets page size.
   *
   * @param pageSize the page size
   */
  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  @Override
  public String toString() {
    return "page:" + page + ",pageSize:" + pageSize;
  }
}
