package org.dalgen.mybatis.generator;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class GeneratorModel implements java.io.Serializable {
  private static final long serialVersionUID = -6430787906037836995L;

  /** 用于存放'模板'可以引用的变量 */
  public Map                templateModel    = new HashMap();

  /** 用于存放'文件路径'可以引用的变量 */
  public Map                filePathModel    = new HashMap();

  public GeneratorModel() {}

  public GeneratorModel(Map templateModel, Map filePathModel) {
    this.templateModel = templateModel;
    this.filePathModel = filePathModel;
  }
}
