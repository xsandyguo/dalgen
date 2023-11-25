package org.dalgen.mybatis.ext.tableconfig;

import java.util.Map;

import org.dalgen.mybatis.util.StringHelper;

class MybatisHelper {
  public static void processMybatisForeachCloseTag(StringBuffer sql, Map preTagAttributes,
      String xmlTag) {
    // mybatis username in <foreach collection="usernameList" item="item" index="index" open="("
    // separator="," close=")">#{item}<foreach>
    if ("/foreach".equals(xmlTag)) {
      // 将 username in (#{item}) 替换为 username in (#collection[]#)
      String item = (String) preTagAttributes.get("item");
      String collection = (String) preTagAttributes.get("collection");
      String tempSql =
          StringHelper.replace(sql.toString(), "#{" + item + "}", "#" + collection + "[]#");
      tempSql =
          StringHelper.replace(tempSql.toString(), "${" + item + "}", "$" + collection + "[]$");
      sql.setLength(0);
      sql.append(tempSql);
    }
  }

  public static void processForMybatis(StringBuffer sb, String xmlTag,
      Map<String, String> attributes) {
    // mybatis <where>
    if ("where".equals(xmlTag)) {
      attributes.put("open", "where");
    }
    // mybatis <set>
    else if ("set".equals(xmlTag)) {
      attributes.put("open", "set");
    }
    // mybatis <foreach collection="usernameList" item="item" index="index" open="(" separator=","
    // close=")">
    else if ("foreach".equals(xmlTag)) {
      // m.appendReplacement(sb, "set"); //FIXME for foreach
    }

    // mybatis <trim prefix="" suffix="" prefixOverrides=""
    // suffixOverrides=""></trim>
    else if ("trim".equals(xmlTag)) {
      attributes.put("open", attributes.get("prefix"));
      attributes.put("close", attributes.get("suffix")); // FIXME for
      // prefixOverrides,suffixOverrides
      // <trim prefix="SET" suffixOverrides=",">
      // <trim prefix="WHERE" prefixOverrides="AND |OR ">
    }
  }
}
