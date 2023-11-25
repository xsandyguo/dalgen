<#include '/java_copyright.include'/>

package ${tableConfig.basepackage}.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * 包含“分页”信息的List
 *
 * <p>要得到总页数请使用 toPaginator().getTotalPages();
 *
 * @param <E> the type parameter
 * @author badqiu
 */
public class PageList<E> extends ArrayList<E> implements Serializable {

  private static final long serialVersionUID = 1412759446332294208L;

  /** 分页大小 */
  private int pageSize;

  /** 页数 */
  private int page;

  /** 总记录数 */
  private int totalItems;

  /** Instantiates a new Page list. */
  public PageList() {}

  /**
   * Instantiates a new Page list.
   *
   * @param c the c
   */
  public PageList(Collection<? extends E> c) {
    super(c);
  }

  /**
   * Instantiates a new Page list.
   *
   * @param page the page
   * @param pageSize the page size
   * @param totalItems the total items
   */
  public PageList(int page, int pageSize, int totalItems) {
    this.page = page;
    this.pageSize = pageSize;
    this.totalItems = totalItems;
  }

  /**
   * Instantiates a new Page list.
   *
   * @param c the c
   * @param page the page
   * @param pageSize the page size
   * @param totalItems the total items
   */
  public PageList(Collection<? extends E> c, int page, int pageSize, int totalItems) {
    super(c);
    this.page = page;
    this.pageSize = pageSize;
    this.totalItems = totalItems;
  }

  /**
   * Instantiates a new Page list.
   *
   * @param p the p
   */
  public PageList(Paginator p) {
    this.page = p.getPage();
    this.pageSize = p.getPageSize();
    this.totalItems = p.getTotalItems();
  }

  /**
   * Instantiates a new Page list.
   *
   * @param c the c
   * @param p the p
   */
  public PageList(Collection<? extends E> c, Paginator p) {
    super(c);
    this.page = p.getPage();
    this.pageSize = p.getPageSize();
    this.totalItems = p.getTotalItems();
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
   * Gets total items.
   *
   * @return the total items
   */
  public int getTotalItems() {
    return totalItems;
  }

  /**
   * Sets total items.
   *
   * @param totalItems the total items
   */
  public void setTotalItems(int totalItems) {
    this.totalItems = totalItems;
  }

  /**
   * 得到分页器，通过Paginator可以得到总页数等值
   *
   * @return paginator
   */
  public Paginator getPaginator() {
    return new Paginator(page, pageSize, totalItems);
  }
}
