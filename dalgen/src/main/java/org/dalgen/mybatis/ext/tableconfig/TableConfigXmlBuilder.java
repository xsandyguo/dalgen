package org.dalgen.mybatis.ext.tableconfig;

import java.io.*;
import java.util.Arrays;
import java.util.List;

import org.dalgen.mybatis.generator.GeneratorConstants;
import org.dalgen.mybatis.generator.GeneratorProperties;
import org.dalgen.mybatis.util.*;
import org.xml.sax.SAXException;

public class TableConfigXmlBuilder {

  public TableConfigSet parseFromXML(String _package, File basedir) {
    String[] tableConfigFilesArray = basedir.list();
    TableConfigSet tableConfigSet = new TableConfigXmlBuilder().parseFromXML(basedir, _package,
        Arrays.asList(tableConfigFilesArray));
    return tableConfigSet;
  }

  public TableConfigSet parseFromXML(String _package, File basedir, String tableConfigFiles) {
    String[] tableConfigFilesArray =
        StringHelper.tokenizeToStringArray(tableConfigFiles, ", \t\n\r\f");
    TableConfigSet tableConfigSet = new TableConfigXmlBuilder().parseFromXML(basedir, _package,
        Arrays.asList(tableConfigFilesArray));
    return tableConfigSet;
  }

  public TableConfigSet parseFromXML(File basedir, String _package, List<String> tableConfigFiles) {
    TableConfigSet result = new TableConfigSet();
    result.setPackage(_package);
    for (String filepath : tableConfigFiles) {
      if (filepath.endsWith(".xml")) {
        File file = new File(basedir, filepath);
        result.addTableConfig(parseFromXML(file));
      }
    }
    return result;
  }

  public TableConfig parseFromXML(File file) {
    InputStream in = null;
    try {
      in = new BufferedInputStream(new FileInputStream(file));
      return parseFromXML(in);
    } catch (Throwable e) {
      throw new RuntimeException("parse file:" + file.getAbsolutePath() + " occer error", e);
    } finally {
      IOHelper.close(in, null);
    }
  }

  public TableConfig parseFromXML(InputStream inputStream) throws SAXException, IOException {
    NodeData nodeData = new XMLHelper().parseXML(inputStream);
    TableConfig config = new TableConfig();

    // table
    BeanHelper.copyProperties(config, nodeData.attributes, true);

    for (NodeData child : nodeData.childs) {
      // table/operation
      if ("operation".equals(child.nodeName)) {
        OperationConfig target = new OperationConfig();
        BeanHelper.copyProperties(target, child.attributes, true);
        for (NodeData opChild : child.childs) {
          // table/operation/extraparams
          if ("extraparams".equals(opChild.nodeName)) {
            // table/operation/extraparams/param
            for (NodeData paramNode : opChild.childs) {
              ParamConfig mp = new ParamConfig();
              BeanHelper.copyProperties(mp, paramNode.attributes, true);
              target.extraparams.add(mp);
            }
          } else {
            BeanHelper.setProperty(target, opChild.nodeName, getNodeValue(opChild));
          }
        }
        config.operations.add(target);
      }
      // table/column
      if ("column".equals(child.nodeName)) {
        ColumnConfig target = new ColumnConfig();
        BeanHelper.copyProperties(target, child.attributes, true);
        config.columns.add(target);
      }
      // table/sql
      if ("sql".equals(child.nodeName)) {
        SqlConfig target = new SqlConfig();
        BeanHelper.copyProperties(target, child.attributes, true);
        target.setSql(getNodeValue(child));
        config.addSqlConfig(target);
      }
      // table/resultmap
      if ("resultmap".equals(child.nodeName)) {
        ResultMapConfig target = new ResultMapConfig();
        BeanHelper.copyProperties(target, child.attributes, true);
        // table/resultmap/column
        for (NodeData c : child.childs) {
          if ("column".equals(c.nodeName)) {
            ColumnConfig column = new ColumnConfig();
            BeanHelper.copyProperties(column, c.attributes, true);
            target.getColumns().add(column);
          }
        }
        config.resultMaps.add(target);
      }
    }
    return config;
  }

  public String getNodeValue(NodeData v) {
    if (GeneratorProperties.getBoolean(GeneratorConstants.USE_INNER_XML_FOR_XML_PARSING)) {
      return v.innerXML;
    } else {
      return v.nodeValue;
    }
  }
}
