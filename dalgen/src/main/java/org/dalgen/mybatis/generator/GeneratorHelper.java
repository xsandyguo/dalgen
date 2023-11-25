package org.dalgen.mybatis.generator;

import java.io.*;
import java.util.*;

import org.dalgen.mybatis.util.AntPathMatcher;
import org.dalgen.mybatis.util.FreemarkerHelper;
import org.dalgen.mybatis.util.StringHelper;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class GeneratorHelper {

  /**
   * 生成 路径值,如 pkg=com.company.project 将生成 pkg_dir=com/company/project的值
   *
   * @param map
   * @return
   */
  public static Map getDirValuesMap(Map map) {
    Map dirValues = new HashMap();
    Set<Object> keys = map.keySet();
    for (Object key : keys) {
      Object value = map.get(key);
      if (key instanceof String && value instanceof String) {
        String dirKey = key + "_dir";
        String dirValue = value.toString().replace('.', '/');
        dirValues.put(dirKey, dirValue);
      }
    }
    return dirValues;
  }

  public static boolean isIgnoreTemplateProcess(File srcFile, String templateFile, String includes,
      String excludes) {
    if (srcFile.isDirectory() || srcFile.isHidden()) {
      return true;
    }
    if (templateFile.trim().equals("")) {
      return true;
    }
    if (srcFile.getName().toLowerCase().endsWith(".include")) {
      log.info("[skip]\t\t endsWith '.include' template:" + templateFile);
      return true;
    }
    templateFile = templateFile.replace('\\', '/');
    for (String exclude : StringHelper.tokenizeToStringArray(excludes, ",")) {
      if (new AntPathMatcher().match(exclude.replace('\\', '/'), templateFile)) {
        return true;
      }
    }
    if (StringHelper.isBlank(includes)) {
      return false;
    }
    for (String include : StringHelper.tokenizeToStringArray(includes, ",")) {
      if (new AntPathMatcher().match(include.replace('\\', '/'), templateFile)) {
        return false;
      }
    }
    return true;
  }

  public static boolean isFoundInsertLocation(GeneratorControl gg, Template template, Map model,
      File outputFile, StringWriter newFileContent) throws IOException, TemplateException {
    LineNumberReader reader = new LineNumberReader(new FileReader(outputFile));
    String line = null;
    boolean isFoundInsertLocation = false;

    // FIXME 持续性的重复生成会导致out of memory
    PrintWriter writer = new PrintWriter(newFileContent);
    while ((line = reader.readLine()) != null) {
      writer.println(line);
      // only insert once
      if (!isFoundInsertLocation && line.indexOf(gg.getMergeLocation()) >= 0) {
        template.process(model, writer);
        writer.println();
        isFoundInsertLocation = true;
      }
    }

    writer.close();
    reader.close();
    return isFoundInsertLocation;
  }

  public static Configuration newFreeMarkerConfiguration(List<File> templateRootDirs,
      String defaultEncoding, String templateName) throws IOException {
    Configuration conf = new Configuration();

    FileTemplateLoader[] templateLoaders = new FileTemplateLoader[templateRootDirs.size()];
    for (int i = 0; i < templateRootDirs.size(); i++) {
      templateLoaders[i] = new FileTemplateLoader((File) templateRootDirs.get(i));
    }
    MultiTemplateLoader multiTemplateLoader = new MultiTemplateLoader(templateLoaders);

    conf.setTemplateLoader(multiTemplateLoader);
    conf.setNumberFormat("###############");
    conf.setBooleanFormat("true,false");
    conf.setDefaultEncoding(defaultEncoding);

    // String autoIncludes = new File(new
    // File(templateName).getParent(),"macro.include").getPath();
    // List<String> availableAutoInclude = FreemarkerHelper.getAvailableAutoInclude(conf,
    // Arrays.asList("macro.include",autoIncludes));
    // conf.setAutoIncludes(availableAutoInclude);
    // log.info("[set Freemarker.autoIncludes]"+availableAutoInclude+" for
    // templateName:"+templateName);

    List<String> autoIncludes = getParentPaths(templateName, "macro.include");
    List<String> availableAutoInclude =
        FreemarkerHelper.getAvailableAutoInclude(conf, autoIncludes);
    conf.setAutoIncludes(availableAutoInclude);
    log.trace("set Freemarker.autoIncludes:" + availableAutoInclude + " for templateName:"
        + templateName + " autoIncludes:" + autoIncludes);
    return conf;
  }

  public static List<String> getParentPaths(String templateName, String suffix) {
    String array[] = StringHelper.tokenizeToStringArray(templateName, "\\/");
    List<String> list = new ArrayList<String>();
    list.add(suffix);
    list.add(File.separator + suffix);
    String path = "";
    for (int i = 0; i < array.length; i++) {
      path = path + File.separator + array[i];
      list.add(path + File.separator + suffix);
    }
    return list;
  }
}
