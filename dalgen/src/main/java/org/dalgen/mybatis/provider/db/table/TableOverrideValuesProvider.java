package org.dalgen.mybatis.provider.db.table;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.dalgen.mybatis.util.FileHelper;
import org.dalgen.mybatis.util.NodeData;
import org.dalgen.mybatis.util.XMLHelper;

import lombok.extern.slf4j.Slf4j;

/** 得到表的自定义配置信息 */
@Slf4j
public class TableOverrideValuesProvider {

  public static Map getTableConfigValues(String tableSqlName) {
    NodeData nd = getTableConfigXmlNodeData(tableSqlName);
    if (nd == null) {
      return new HashMap();
    }
    return nd == null ? new HashMap() : nd.attributes;
  }

  public static Map getColumnConfigValues(Table table, Column column) {
    NodeData root = getTableConfigXmlNodeData(table.getSqlName());
    if (root != null) {
      for (NodeData item : root.childs) {
        if (item.nodeName.equals("column")) {
          if (column.getSqlName().equalsIgnoreCase(item.attributes.get("sqlName"))) {
            return item.attributes;
          }
        }
      }
    }
    return new HashMap();
  }

  public static NodeData getTableConfigXmlNodeData(String tableSqlName) {
    NodeData nd = getTableConfigXmlNodeData0(tableSqlName);
    if (nd == null) {
      nd = getTableConfigXmlNodeData0(tableSqlName.toLowerCase());
      if (nd == null) {
        nd = getTableConfigXmlNodeData0(tableSqlName.toUpperCase());
      }
    }
    return nd;
  }

  public static NodeData getTableConfigXmlNodeData0(String tableSqlName) {
    try {
      File file =
          FileHelper.getFileByClassLoader("generator_config/table/" + tableSqlName + ".xml");
      log.trace("getTableConfigXml() load nodeData by tableSqlName:" + tableSqlName + ".xml");
      return new XMLHelper().parseXML(file);
    } catch (Exception e) { // ignore
      log.trace("not found config xml for table:" + tableSqlName + ", exception:" + e);
      return null;
    }
  }
}
