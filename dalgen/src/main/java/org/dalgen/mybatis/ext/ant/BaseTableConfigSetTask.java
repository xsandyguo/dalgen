package org.dalgen.mybatis.ext.ant;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.dalgen.mybatis.ext.tableconfig.TableConfigSet;
import org.dalgen.mybatis.ext.tableconfig.TableConfigXmlBuilder;

/**
 * 如果要使用TableConfigSet的GeneratorTask，则可以继承该类
 *
 * @author badqiu
 */
public abstract class BaseTableConfigSetTask extends BaseGeneratorTask {

  private static ThreadLocal<Map> threadLocalCache = new ThreadLocal<Map>();
  protected TableConfigSet        tableConfigSet;
  private String                  tableConfigFiles;

  public static Map getThreadLocalCache() {
    Map map = threadLocalCache.get();
    if (map == null) {
      map = new HashMap();
      threadLocalCache.set(map);
    }
    return map;
  }

  static TableConfigSet parseForTableConfigSet(String _package, File basedir,
      String tableConfigFiles) {
    return new TableConfigXmlBuilder().parseFromXML(_package, basedir, tableConfigFiles);
  }

  @Override
  protected void executeBefore() throws Exception {
    super.executeBefore();

    if (tableConfigFiles == null || "".equals(tableConfigFiles.trim())) {
      throw new Exception("'tableConfigFiles' must be not null");
    }

    if (tableConfigSet == null) {
      Map cache = getThreadLocalCache();
      tableConfigSet = (TableConfigSet) cache.get(tableConfigFiles);
      if (tableConfigSet == null) {
        tableConfigSet = parseForTableConfigSet(getPackage(),
            getProject().getBaseDir().getAbsoluteFile(), tableConfigFiles);
      }
      cache.put(tableConfigFiles, tableConfigSet);
    }
  }

  public void setTableConfigFiles(String tableConfigFiles) {
    this.tableConfigFiles = tableConfigFiles;
  }
}
