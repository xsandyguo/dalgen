package org.dalgen.mybatis.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class NodeData {
  public String                        nodeName;
  public String                        nodeValue;
  public String                        innerXML;
  public String                        outerXML;
  // public String innerText;
  // public String outerText;
  public LinkedHashMap<String, String> attributes = new LinkedHashMap<String, String>();
  public List<NodeData>                childs     = new ArrayList<NodeData>();

  public LinkedHashMap<String, String> nodeNameAsAttributes(String nodeNameKey) {
    LinkedHashMap map = new LinkedHashMap();
    map.putAll(attributes);
    map.put(nodeNameKey, nodeName);
    return map;
  }

  public List<LinkedHashMap<String, String>> childsAsListMap() {
    List<LinkedHashMap<String, String>> result = new ArrayList();
    for (NodeData c : childs) {
      LinkedHashMap map = new LinkedHashMap();
      map.put(c.nodeName, c.nodeValue);
      result.add(map);
    }
    return result;
  }

  @Override
  public String toString() {
    return "nodeName=" + nodeName + ",attributes=" + attributes + " nodeValue=" + nodeValue
        + " child:\n" + childs;
  }
}
