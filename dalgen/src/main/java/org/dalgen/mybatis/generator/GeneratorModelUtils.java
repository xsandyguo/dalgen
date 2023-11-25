package org.dalgen.mybatis.generator;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.dalgen.mybatis.util.BeanHelper;
import org.dalgen.mybatis.util.ClassHelper;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class GeneratorModelUtils {

  public static GeneratorModel newGeneratorModel(String key, Object valueObject) {
    GeneratorModel gm = newDefaultGeneratorModel();
    gm.templateModel.put(key, valueObject);
    gm.filePathModel.putAll(BeanHelper.describe(valueObject));
    return gm;
  }

  public static GeneratorModel newFromMap(Map params) {
    GeneratorModel gm = newDefaultGeneratorModel();
    gm.templateModel.putAll(params);
    gm.filePathModel.putAll(params);
    return gm;
  }

  public static GeneratorModel newDefaultGeneratorModel() {
    Map templateModel = new HashMap();
    templateModel.putAll(getShareVars());

    Map filePathModel = new HashMap();
    filePathModel.putAll(getShareVars());
    return new GeneratorModel(templateModel, filePathModel);
  }

  public static Map getShareVars() {
    Map templateModel = new HashMap();
    templateModel.putAll(System.getProperties());
    templateModel.putAll(GeneratorProperties.getProperties());
    templateModel.put("env", System.getenv());
    templateModel.put("now", new Date());
    templateModel.put(GeneratorConstants.DATABASE_TYPE.code,
        GeneratorProperties.getDatabaseType(GeneratorConstants.DATABASE_TYPE.code));
    templateModel.putAll(GeneratorContext.getContext());
    templateModel.putAll(getToolsMap());
    return templateModel;
  }

  /** 得到模板可以引用的工具类 */
  private static Map getToolsMap() {
    Map toolsMap = new HashMap();
    String[] tools = GeneratorProperties.getStringArray(GeneratorConstants.GENERATOR_TOOLS_CLASS);
    for (String className : tools) {
      try {
        Object instance = ClassHelper.newInstance(className);
        toolsMap.put(Class.forName(className).getSimpleName(), instance);
        log.debug("put tools class:" + className + " with key:"
            + Class.forName(className).getSimpleName());
      } catch (Exception e) {
        log.error("cannot load tools by className:" + className + " cause:" + e);
      }
    }
    return toolsMap;
  }
}
