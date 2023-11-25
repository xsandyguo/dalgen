package org.dalgen.mybatis.ext.ant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dalgen.mybatis.util.BeanHelper;

public class TableConfigSetGenTask extends BaseTableConfigSetTask {

  @Override
  protected List<Map> getGeneratorContexts() {
    Map map = new HashMap();
    map.putAll(BeanHelper.describe(tableConfigSet));
    map.put("tableConfigSet", tableConfigSet);
    return Arrays.asList(map);
  }
}
