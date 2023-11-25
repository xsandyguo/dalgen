package org.dalgen.mybatis.ext.tableconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.dalgen.mybatis.provider.db.sql.SqlSegment;
import org.dalgen.mybatis.sqlparse.SqlParseHelper;
import org.dalgen.mybatis.util.StringHelper;
import org.dalgen.mybatis.util.XMLHelper;

/**
 * 解析ibatis的sql语句,并转换成正常的sql.
 *
 * <p>
 * 主要功能: 1.将动态构造条件节点全部替换成删除 只留下可执行的SQL
 */
public class IbatisSqlMapConfigParser {
  public Map<String, SqlSegment> usedIncludedSqls = new HashMap(); // 增加一条sql语句引用的 includesSql的解析
  public String                  resultSql;
  private String                 sourceSql;
  private Map<String, String>    includeSqls;

  public String parse(String str) {
    return parse(str, new HashMap());
  }

  public String parse(String str, Map<String, String> includeSqls) {
    this.sourceSql = str;
    this.includeSqls = includeSqls;
    str = Helper.removeComments("<for_remove_comment>" + str + "</for_remove_comment>");
    str = Helper.removeSelectKeyXmlForInsertSql(str);

    Pattern xmlTagRegex = Pattern.compile("<(/?[\\w#@]+)(.*?)>");
    StringBuffer sql = new StringBuffer();
    Matcher m = xmlTagRegex.matcher(str);

    OpenCloseTag openClose = null;
    Map previousTagAttributes = null;
    while (m.find()) {
      String xmlTag = StringUtils.trim(m.group(1));
      String attributesString = m.group(2);
      Map<String, String> attributes = XMLHelper.parse2Attributes(attributesString);

      if ("include".equals(xmlTag)) {
        processIncludeByRefid(includeSqls, sql, m, attributes);
        continue;
      }

      MybatisHelper.processForMybatis(sql, xmlTag, attributes);

      String replacement = Helper.getReplacement(attributes.get("open"), attributes.get("prepend"));
      StringHelper.appendReplacement(m, sql, replacement);

      openClose = processOpenClose(sql, openClose, xmlTag, attributes);

      MybatisHelper.processMybatisForeachCloseTag(sql, previousTagAttributes, xmlTag);

      previousTagAttributes = attributes;
    }
    // FIXME 不能兼容自动删除分号, 因为还需要测试最终的ibatis sql是否会删除;
    resultSql = StringHelper
        .unescapeXml(StringHelper.removeXMLCdataTag(SqlParseHelper.replaceWhere(sql.toString())));
    return resultSql;
    // return
    // StringHelper.unescapeXml(StringHelper.removeXMLCdataTag(SqlParseHelper.replaceWhere(sql.toString()))).replace(";",
    // "");
  }

  private OpenCloseTag processOpenClose(StringBuffer sql, OpenCloseTag openClose, String xmlTag,
      Map<String, String> attributes) {
    if (openClose != null && openClose.close != null && xmlTag.equals("/" + openClose.xmlTag)) {
      sql.append(openClose.close);
      openClose = null;
    }
    if (attributes.get("close") != null) {
      openClose = new OpenCloseTag(); // FIXME 未处理多个open close问题
      openClose.xmlTag = xmlTag;
      openClose.close = attributes.get("close");
    }
    return openClose;
  }

  public List<SqlSegment> getSqlSegments() {
    return new ArrayList(usedIncludedSqls.values());
  }

  // process <include refid="otherSql"/>
  private void processIncludeByRefid(Map<String, String> includeSqls, StringBuffer sb, Matcher m,
      Map<String, String> attributes) {
    String refid = attributes.get("refid");
    if (refid == null) {
      m.appendReplacement(sb, "");
    } else {
      String includeValue = includeSqls.get(refid);
      if (includeValue == null)
        throw new IllegalArgumentException(
            "not found include sql by <include refid='" + refid + "'/>");
      String parsedIncludeValue = parse(includeValue, includeSqls);
      usedIncludedSqls.put(refid, new SqlSegment(refid, includeValue, parsedIncludeValue));
      StringHelper.appendReplacement(m, sb, parsedIncludeValue);
    }
  }
}
