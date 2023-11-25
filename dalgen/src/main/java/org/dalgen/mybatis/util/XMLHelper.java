package org.dalgen.mybatis.util;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.*;
import org.w3c.dom.CharacterData;
import org.xml.sax.SAXException;

/**
 * 将xml解析成NodeData,NodeData主要是使用Map及List来装attribute
 *
 * <p>
 * 使用:
 *
 * <pre>
 *     NodeData nd = XMLHelper.parseXML(inputStream)
 * </pre>
 *
 * @author badqiu
 */
public class XMLHelper {

  public static LinkedHashMap<String, String> attrbiuteToMap(NamedNodeMap attributes) {
    if (attributes == null) {
      return new LinkedHashMap<String, String>();
    }
    LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
    for (int i = 0; i < attributes.getLength(); i++) {
      result.put(attributes.item(i).getNodeName(), attributes.item(i).getNodeValue());
    }
    return result;
  }

  /**
   * Extract the text value from the given DOM element, ignoring XML comments.
   *
   * <p>
   * Appends all CharacterData nodes and EntityReference nodes into a single String value, excluding
   * Comment nodes.
   *
   * @see CharacterData
   * @see EntityReference
   * @see Comment
   */
  public static String getTextValue(Element valueEle) {
    if (valueEle == null) {
      throw new IllegalArgumentException("Element must not be null");
    }
    StringBuilder sb = new StringBuilder();
    NodeList nl = valueEle.getChildNodes();
    for (int i = 0; i < nl.getLength(); i++) {
      Node item = nl.item(i);
      if ((item instanceof CharacterData && !(item instanceof Comment))
          || item instanceof EntityReference) {
        sb.append(item.getNodeValue());
      } else if (item instanceof Element) {
        sb.append(getTextValue((Element) item));
      }
    }
    return sb.toString();
  }

  public static String getNodeValue(Node node) {
    if (node instanceof Comment) {
      return null;
    }
    if (node instanceof CharacterData) {
      return ((CharacterData) node).getData();
    }
    if (node instanceof EntityReference) {
      return node.getNodeValue();
    }
    if (node instanceof Element) {
      return getTextValue((Element) node);
    }
    return node.getNodeValue();
  }

  public static String getXMLEncoding(InputStream inputStream)
      throws UnsupportedEncodingException, IOException {
    return getXMLEncoding(IOHelper.toString("UTF-8", inputStream));
  }

  public static String getXMLEncoding(String s) {
    if (s == null) {
      return null;
    }
    Pattern p = Pattern.compile("<\\?xml.*encoding=[\"'](.*)[\"']\\?>");
    Matcher m = p.matcher(s);
    if (m.find()) {
      return m.group(1);
    }
    return null;
  }

  public static String removeXmlns(File file) throws IOException {
    InputStream forEncodingInput = new FileInputStream(file);
    String encoding = XMLHelper.getXMLEncoding(forEncodingInput);
    forEncodingInput.close();

    InputStream input = new FileInputStream(file);
    String xml = IOHelper.toString(encoding, input);
    xml = XMLHelper.removeXmlns(xml);
    input.close();
    return xml;
  }

  // 只移除default namesapce
  public static String removeXmlns(String s) {
    if (s == null) {
      return null;
    }
    s = s.replaceAll("(?s)xmlns=['\"].*?['\"]", "");
    // s = s.replaceAll("(?s)xmlns:?\\w*=['\"].*?['\"]", "");
    s = s.replaceAll("(?s)\\w*:schemaLocation=['\"].*?['\"]", "");
    return s;
  }

  /**
   * 解析attributes为hashMap
   *
   * @param attributes 格式： name='badqiu' sex='F'
   * @return
   */
  public static LinkedHashMap<String, String> parse2Attributes(String attributes) {
    LinkedHashMap result = new LinkedHashMap();
    Pattern p = Pattern.compile("(\\w+?)=['\"](.*?)['\"]");
    Matcher m = p.matcher(attributes);
    while (m.find()) {
      result.put(m.group(1), StringHelper.unescapeXml(m.group(2)));
    }
    return result;
  }

  public static void main(String[] args) throws FileNotFoundException, SAXException, IOException {
    String file = "D:/dev/workspaces/alipay/ali-generator/generator/src/table_test.xml";
    NodeData nd = new XMLHelper().parseXML(new FileInputStream(new File(file)));

    LinkedHashMap table = nd.attributes;
    List columns = nd.childs;
    System.out.println(table);
    System.out.println(columns);
    // System.out.println(nd);
  }

  public NodeData parseXML(InputStream in) throws SAXException, IOException {
    Document doc = SimpleXmlParser.getLoadingDoc(in);
    return SimpleXmlParser.treeWalk(doc.getDocumentElement());
  }

  public NodeData parseXML(File file) throws SAXException, IOException {
    FileInputStream in = new FileInputStream(file);
    try {
      return parseXML(in);
    } finally {
      in.close();
    }
  }

}
