package org.dalgen.mybatis.util;

import java.io.*;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SimpleXmlParser {
  public static Document getLoadingDoc(String file)
      throws FileNotFoundException, SAXException, IOException {
    return getLoadingDoc(new FileInputStream(file));
  }

  static Document getLoadingDoc(InputStream in) throws SAXException, IOException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setIgnoringElementContentWhitespace(false);
    dbf.setValidating(false);
    dbf.setCoalescing(false); // convert CDATA nodes to Text
    dbf.setIgnoringComments(false); // 为false时与CDATA冲突
    try {
      DocumentBuilder db = dbf.newDocumentBuilder();

      // ignore entity resolver
      db.setEntityResolver(new EntityResolver() {
        @Override
        public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
          InputSource is = new InputSource(new StringReader(""));
          is.setSystemId(systemId);
          return is;
        }
      });

      InputSource is = new InputSource(in);
      return db.parse(is);
    } catch (ParserConfigurationException x) {
      throw new Error(x);
    }
  }

  public static NodeData treeWalk(Element elm) {
    NodeData nodeData = new NodeData();
    nodeData.attributes = XMLHelper.attrbiuteToMap(elm.getAttributes());
    nodeData.nodeName = elm.getNodeName();
    nodeData.childs = new ArrayList<NodeData>();
    nodeData.innerXML = childsAsText(elm, new StringBuffer(), true).toString();
    nodeData.outerXML = nodeAsText(elm, new StringBuffer(), true).toString();
    nodeData.nodeValue = XMLHelper.getNodeValue(elm);
    // nodeData.innerText = childsAsText(elm, new StringBuffer(),false).toString();
    // nodeData.outerText = nodeAsText(elm,new StringBuffer(),false).toString();
    NodeList childs = elm.getChildNodes();
    for (int i = 0; i < childs.getLength(); i++) {
      Node node = childs.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        nodeData.childs.add(treeWalk((Element) node));
      }
    }
    return nodeData;
  }

  private static StringBuffer childsAsText(Element elm, StringBuffer sb, boolean ignoreComments) {
    NodeList childs = elm.getChildNodes();
    for (int i = 0; i < childs.getLength(); i++) {
      Node child = childs.item(i);
      nodeAsText(child, sb, ignoreComments);
    }
    return sb;
  }

  private static StringBuffer nodeAsText(Node elm, StringBuffer sb, boolean ignoreComments) {
    if (elm.getNodeType() == Node.CDATA_SECTION_NODE) {
      CDATASection cdata = (CDATASection) elm;
      sb.append("<![CDATA[");
      sb.append(cdata.getData());
      sb.append("]]>");
      return sb;
    }
    if (elm.getNodeType() == Node.COMMENT_NODE) {
      if (ignoreComments) {
        return sb;
      }
      Comment c = (Comment) elm;
      sb.append("<!--");
      sb.append(c.getData());
      sb.append("-->");
      return sb;
    }
    if (elm.getNodeType() == Node.TEXT_NODE) {
      Text t = (Text) elm;
      sb.append(StringHelper.escapeXml(t.getData(), "<&"));
      return sb;
    }
    NodeList childs = elm.getChildNodes();
    sb.append("<" + elm.getNodeName());
    attributes2String(elm, sb);
    if (childs.getLength() > 0) {
      sb.append(">");
      for (int i = 0; i < childs.getLength(); i++) {
        Node child = childs.item(i);
        nodeAsText(child, sb, ignoreComments);
      }
      sb.append("</" + elm.getNodeName() + ">");
    } else {
      sb.append("/>");
    }
    return sb;
  }

  private static void attributes2String(Node elm, StringBuffer sb) {
    NamedNodeMap attributes = elm.getAttributes();
    if (attributes != null && attributes.getLength() > 0) {
      sb.append(" ");
      for (int j = 0; j < attributes.getLength(); j++) {
        sb.append(String.format("%s=\"%s\"", attributes.item(j).getNodeName(),
            StringHelper.escapeXml(attributes.item(j).getNodeValue(), "<&\"")));
        if (j < attributes.getLength() - 1) {
          sb.append(" ");
        }
      }
    }
  }
}
