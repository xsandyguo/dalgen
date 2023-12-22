package org.dalgen.mybatis.generator;

import java.io.*;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.dalgen.mybatis.util.*;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

/**
 * 代码生成器核心引擎 主要提供以下两个方法供外部使用
 *
 * <pre>
 * generateBy() 用于生成文件
 * deleteBy() 用于删除生成的文件
 * </pre>
 *
 * @author badqiu
 * @email badqiu(a)gmail.com
 */
@SuppressWarnings("all")
@Slf4j
public class Generator {
  private static final String GENERATOR_INSERT_LOCATION       = "generator-insert-location";
  private static final String GNENERATOR_NONE_OVERRIDE="!";
  private ArrayList<File>     templateRootDirs                = new ArrayList<File>();
  private String              outRootDir;
  private boolean             ignoreTemplateGenerateException = true;
  private String              removeExtensions                =
      GeneratorProperties.getProperty(GeneratorConstants.GENERATOR_REMOVE_EXTENSIONS);
  private boolean             isCopyBinaryFile                = true;

  private String              includes                        =
      GeneratorProperties.getProperty(GeneratorConstants.GENERATOR_INCLUDES);               // 需要处理的模板，使用逗号分隔符,示例值:
                                                                                            // java_src/**,java_test/**
  private String              excludes                        =
      GeneratorProperties.getProperty(GeneratorConstants.GENERATOR_EXCLUDES);               // 不需要处理的模板，使用逗号分隔符,示例值:
                                                                                            // java_src/**,java_test/**
  private String              sourceEncoding                  =
      GeneratorProperties.getProperty(GeneratorConstants.GENERATOR_SOURCE_ENCODING);
  private String              outputEncoding                  =
      GeneratorProperties.getProperty(GeneratorConstants.GENERATOR_OUTPUT_ENCODING);



  public Generator() {}

  public void setTemplateRootDir(File templateRootDir) {
    setTemplateRootDirs(new File[] {templateRootDir});
  }

  /**
   * 设置模板目录，支持用逗号分隔多个模板目录，如 template/rapid,template/company
   *
   * @param templateRootDir
   */
  public void setTemplateRootDir(String templateRootDir) {
    setTemplateRootDirs(StringHelper.tokenizeToStringArray(templateRootDir, ","));
  }

  public void setTemplateRootDirs(File... templateRootDirs) {
    this.templateRootDirs = new ArrayList<File>(Arrays.asList(templateRootDirs));
  }

  public void setTemplateRootDirs(String... templateRootDirs) {
    ArrayList<File> tempDirs = new ArrayList<File>();
    for (String dir : templateRootDirs) {
      tempDirs.add(FileHelper.getFile(dir));
    }
    this.templateRootDirs = tempDirs;
  }

  public void addTemplateRootDir(File file) {
    templateRootDirs.add(file);
  }

  public void addTemplateRootDir(String file) {
    templateRootDirs.add(FileHelper.getFile(file));
  }

  public boolean isIgnoreTemplateGenerateException() {
    return ignoreTemplateGenerateException;
  }

  public void setIgnoreTemplateGenerateException(boolean ignoreTemplateGenerateException) {
    this.ignoreTemplateGenerateException = ignoreTemplateGenerateException;
  }

  public boolean isCopyBinaryFile() {
    return isCopyBinaryFile;
  }

  public void setCopyBinaryFile(boolean isCopyBinaryFile) {
    this.isCopyBinaryFile = isCopyBinaryFile;
  }

  public String getSourceEncoding() {
    return sourceEncoding;
  }

  public void setSourceEncoding(String sourceEncoding) {
    if (StringHelper.isBlank(sourceEncoding))
      throw new IllegalArgumentException("sourceEncoding must be not empty");
    this.sourceEncoding = sourceEncoding;
  }

  public String getOutputEncoding() {
    return outputEncoding;
  }

  public void setOutputEncoding(String outputEncoding) {
    if (StringHelper.isBlank(outputEncoding))
      throw new IllegalArgumentException("outputEncoding must be not empty");
    this.outputEncoding = outputEncoding;
  }

  public void setIncludes(String includes) {
    this.includes = includes;
  }

  /** 设置不处理的模板路径,可以使用ant类似的值,使用逗号分隔，示例值： **\*.ignore */
  public void setExcludes(String excludes) {
    this.excludes = excludes;
  }

  public String getOutRootDir() {
    // if(outRootDir == null) throw new IllegalStateException("'outRootDir' property must be not
    // null.");
    return outRootDir;
  }

  public void setOutRootDir(String rootDir) {
    if (rootDir == null)
      throw new IllegalArgumentException("outRootDir must be not null");
    this.outRootDir = rootDir;
  }

  public void setRemoveExtensions(String removeExtensions) {
    this.removeExtensions = removeExtensions;
  }

  public void deleteOutRootDir() throws IOException {
    if (StringHelper.isBlank(getOutRootDir()))
      throw new IllegalStateException("'outRootDir' property must be not null.");
    log.info("[delete dir]    " + getOutRootDir());
    FileHelper.deleteDirectory(new File(getOutRootDir()));
  }

  /**
   * 生成文件
   *
   * @param templateModel 生成器模板可以引用的变量
   * @param filePathModel 文件路径可以引用的变量
   * @throws Exception
   */
  public Generator generateBy(Map templateModel, Map filePathModel) throws Exception {
    processTemplateRootDirs(templateModel, filePathModel, false);
    return this;
  }

  /**
   * 删除生成的文件
   *
   * @param templateModel 生成器模板可以引用的变量
   * @param filePathModel 文件路径可以引用的变量
   * @return
   * @throws Exception
   */
  public Generator deleteBy(Map templateModel, Map filePathModel) throws Exception {
    processTemplateRootDirs(templateModel, filePathModel, true);
    return this;
  }

  private void processTemplateRootDirs(Map templateModel, Map filePathModel, boolean isDelete)
      throws Exception {
    if (StringHelper.isBlank(getOutRootDir()))
      throw new IllegalStateException("'outRootDir' property must be not empty.");
    if (templateRootDirs == null || templateRootDirs.size() == 0)
      throw new IllegalStateException("'templateRootDirs'  must be not empty");

    log.debug("******* Template reference variables *********", templateModel);
    log.debug("\n\n******* FilePath reference variables *********", filePathModel);

    // 生成 路径值,如 pkg=com.company.project 将生成 pkg_dir=com/company/project的值
    templateModel.putAll(GeneratorHelper.getDirValuesMap(templateModel));
    filePathModel.putAll(GeneratorHelper.getDirValuesMap(filePathModel));

    GeneratorException ge = new GeneratorException(
        "generator occer error, Generator BeanInfo:" + BeanHelper.describe(this));
    List<File> processedTemplateRootDirs = processTemplateRootDirs();

    for (int i = 0; i < processedTemplateRootDirs.size(); i++) {
      File templateRootDir = (File) processedTemplateRootDirs.get(i);
      List<Exception> exceptions = scanTemplatesAndProcess(templateRootDir,
          processedTemplateRootDirs, templateModel, filePathModel, isDelete);
      ge.addAll(exceptions);
    }
    if (!ge.exceptions.isEmpty())
      throw ge;
  }

  /** 用于子类覆盖,预处理模板目录,如执行文件解压动作 */
  protected List<File> processTemplateRootDirs() throws Exception {
    return unzipIfTemplateRootDirIsZipFile();
  }

  /**
   * 解压模板目录,如果模板目录是一个zip,jar文件 . 并且支持指定 zip文件的子目录作为模板目录,通过 !号分隔 指定zip文件: c:\\some.zip 指定zip文件子目录:
   * c:\some.zip!/folder/
   *
   * @throws MalformedURLException
   */
  private List<File> unzipIfTemplateRootDirIsZipFile() throws MalformedURLException {
    List<File> unzipIfTemplateRootDirIsZipFile = new ArrayList<File>();
    for (int i = 0; i < this.templateRootDirs.size(); i++) {
      File file = templateRootDirs.get(i);
      String templateRootDir = FileHelper.toFilePathIfIsURL(file);

      String subFolder = "";
      int zipFileSeperatorIndexOf = templateRootDir.indexOf("!");
      if (zipFileSeperatorIndexOf >= 0) {
        subFolder = templateRootDir.substring(zipFileSeperatorIndexOf + 1);
        templateRootDir = templateRootDir.substring(0, zipFileSeperatorIndexOf);
      }

      if (new File(templateRootDir).isFile()) {
        File tempDir = ZipUtils.unzip2TempDir(new File(templateRootDir),
            "tmp_generator_template_folder_for_zipfile");
        unzipIfTemplateRootDirIsZipFile.add(new File(tempDir, subFolder));
      } else {
        unzipIfTemplateRootDirIsZipFile.add(new File(templateRootDir, subFolder));
      }
    }
    return unzipIfTemplateRootDirIsZipFile;
  }

  /**
   * 搜索templateRootDir目录下的所有文件并生成东西
   *
   * @param templateRootDir 用于搜索的模板目录
   * @param templateRootDirs freemarker用于装载模板的目录
   */
  private List<Exception> scanTemplatesAndProcess(File templateRootDir, List<File> templateRootDirs,
      Map templateModel, Map filePathModel, boolean isDelete) throws Exception {
    if (templateRootDir == null)
      throw new IllegalStateException("'templateRootDir' must be not null");
    log.info("-------------------load template from templateRootDir = '"
        + templateRootDir.getAbsolutePath() + "' outRootDir:"
        + new File(outRootDir).getAbsolutePath());

    List srcFiles = FileHelper.searchAllNotIgnoreFile(templateRootDir);

    List<Exception> exceptions = new ArrayList();
    for (int i = 0; i < srcFiles.size(); i++) {
      File srcFile = (File) srcFiles.get(i);
      try {
        if (isDelete) {
          new TemplateProcessor(templateRootDirs).executeDelete(templateRootDir, templateModel,
              filePathModel, srcFile);
        } else {
          long start = System.currentTimeMillis();
          new TemplateProcessor(templateRootDirs).executeGenerate(templateRootDir, templateModel,
              filePathModel, srcFile);
          log.info("genereate by tempate cost time:" + (System.currentTimeMillis() - start) + "ms");
        }
      } catch (Exception e) {
        if (ignoreTemplateGenerateException) {
          log.warn("iggnore generate error,template is:" + srcFile + " cause:" + e);
          exceptions.add(e);
        } else {
          throw e;
        }
      }
    }
    return exceptions;
  }

  /** 单个模板文件的处理器 */
  private class TemplateProcessor {
    private GeneratorControl gg               = new GeneratorControl();
    private List<File>       templateRootDirs = new ArrayList<File>();

    public TemplateProcessor(List<File> templateRootDirs) {
      super();
      this.templateRootDirs = templateRootDirs;
    }

    private void executeGenerate(File templateRootDir, Map templateModel, Map filePathModel,
        File srcFile) throws SQLException, IOException, TemplateException {
      String templateFile = FileHelper.getRelativePath(templateRootDir, srcFile);
      if (GeneratorHelper.isIgnoreTemplateProcess(srcFile, templateFile, includes, excludes)) {
        return;
      }

      if (isCopyBinaryFile && FileHelper.isBinaryFile(srcFile)) {
        String outputFilepath = proceeForOutputFilepath(filePathModel, templateFile);
        File outputFile = new File(getOutRootDir(), outputFilepath);
        log.info("[copy binary file by extention] from:" + srcFile + " => " + outputFile);
        FileHelper.parentMkdir(outputFile);
        IOHelper.copyAndClose(new FileInputStream(srcFile), new FileOutputStream(outputFile));
        return;
      }

      try {
        String outputFilepath = proceeForOutputFilepath(filePathModel, templateFile);

        initGeneratorControlProperties(srcFile, outputFilepath);
        processTemplateForGeneratorControl(templateModel, templateFile);

        if (gg.isIgnoreOutput()) {
          log.info("[not generate] by gg.isIgnoreOutput()=true on template:" + templateFile);
          return;
        }

        if (StringHelper.isNotBlank(gg.getOutputFile())) {
          generateNewFileOrInsertIntoFile(templateFile, gg.getOutputFile(), templateModel);
        }
      } catch (Exception e) {
        throw new RuntimeException("generate oucur error,templateFile is:" + templateFile + " => "
            + gg.getOutputFile() + " cause:" + e, e);
      }
    }

    private void executeDelete(File templateRootDir, Map templateModel, Map filePathModel,
        File srcFile) throws SQLException, IOException, TemplateException {
      String templateFile = FileHelper.getRelativePath(templateRootDir, srcFile);
      if (GeneratorHelper.isIgnoreTemplateProcess(srcFile, templateFile, includes, excludes)) {
        return;
      }
      String outputFilepath = proceeForOutputFilepath(filePathModel, templateFile);
      initGeneratorControlProperties(srcFile, outputFilepath);
      gg.deleteGeneratedFile = true;
      processTemplateForGeneratorControl(templateModel, templateFile);
      log.info("[delete file] file:" + new File(gg.getOutputFile()).getAbsolutePath());
      new File(gg.getOutputFile()).delete();
    }

    private void initGeneratorControlProperties(File srcFile, String outputFile)
        throws SQLException {
      if(StringUtils.endsWith(outputFile, GNENERATOR_NONE_OVERRIDE)){
        outputFile = StringUtils.removeEnd(outputFile, GNENERATOR_NONE_OVERRIDE);
        gg.setOverride(false);
      }

      gg.setSourceFile(srcFile.getAbsolutePath());
      gg.setSourceFileName(srcFile.getName());
      gg.setSourceDir(srcFile.getParent());
      gg.setOutRoot(getOutRootDir());
      gg.setOutputEncoding(outputEncoding);
      gg.setSourceEncoding(sourceEncoding);
      gg.setMergeLocation(GENERATOR_INSERT_LOCATION);
      gg.setOutputFile(outputFile);
    }

    private void processTemplateForGeneratorControl(Map templateModel, String templateFile)
        throws IOException, TemplateException {
      templateModel.put("gg", gg);
      Template template = getFreeMarkerTemplate(templateFile);
      template.process(templateModel, IOHelper.NULL_WRITER);
    }

    /** 处理文件路径的变量变成输出路径 */
    private String proceeForOutputFilepath(Map filePathModel, String templateFile)
        throws IOException {
      String outputFilePath = templateFile;

      // TODO 删除兼容性的@testExpression
      int testExpressionIndex = -1;
      if ((testExpressionIndex = templateFile.indexOf('@')) != -1) {
        outputFilePath = templateFile.substring(0, testExpressionIndex);
        String testExpressionKey = templateFile.substring(testExpressionIndex + 1);
        Object expressionValue = filePathModel.get(testExpressionKey);
        if (expressionValue == null) {
          System.err.println("[not-generate] WARN: test expression is null by key:["
              + testExpressionKey + "] on template:[" + templateFile + "]");
          return null;
        }
        if (!"true".equals(String.valueOf(expressionValue))) {
          log.info("[not-generate]\t test expression '@" + testExpressionKey
              + "' is false,template:" + templateFile);
          return null;
        }
      }

      for (String removeExtension : removeExtensions.split(",")) {
        if (outputFilePath.endsWith(removeExtension)) {
          outputFilePath =
              outputFilePath.substring(0, outputFilePath.length() - removeExtension.length());
          break;
        }
      }
      Configuration conf = GeneratorHelper.newFreeMarkerConfiguration(templateRootDirs,
          sourceEncoding, "/filepath/processor/");

      // 使freemarker支持过滤,如 ${className?lower_case} 现在为 ${className^lower_case}
      outputFilePath = outputFilePath.replace('^', '?');
      return FreemarkerHelper.processTemplateString(outputFilePath, filePathModel, conf);
    }

    private Template getFreeMarkerTemplate(String templateName) throws IOException {
      return GeneratorHelper
          .newFreeMarkerConfiguration(templateRootDirs, sourceEncoding, templateName)
          .getTemplate(templateName);
    }

    private void generateNewFileOrInsertIntoFile(String templateFile, String outputFilepath,
        Map templateModel) throws Exception {
      Template template = getFreeMarkerTemplate(templateFile);
      template.setOutputEncoding(gg.getOutputEncoding());

      File absoluteOutputFilePath = FileHelper.parentMkdir(outputFilepath);
      if (absoluteOutputFilePath.exists()) {
        StringWriter newFileContentCollector = new StringWriter();
        if (GeneratorHelper.isFoundInsertLocation(gg, template, templateModel,
            absoluteOutputFilePath, newFileContentCollector)) {
          log.info("[insert]\t generate content into:" + outputFilepath);
          IOHelper.saveFile(absoluteOutputFilePath, newFileContentCollector.toString(),
              gg.getOutputEncoding());
          return;
        }
      }

      if (absoluteOutputFilePath.exists() && !gg.isOverride()) {
        log.info(
            "[not generate]\t by gg.isOverride()=false and outputFile exist:" + outputFilepath);
        return;
      }

      if (absoluteOutputFilePath.exists()) {
        log.info("[override]\t template:" + templateFile + " ==> " + outputFilepath);
      } else {
        log.info("[generate]\t template:" + templateFile + " ==> " + outputFilepath);
      }

      FreemarkerHelper.processTemplate(template, templateModel, absoluteOutputFilePath,
          gg.getOutputEncoding());
    }
  }
}
