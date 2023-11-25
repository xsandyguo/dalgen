package org.dalgen.mybatis.bootstrap;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

import org.dalgen.mybatis.ext.tableconfig.TableConfigSet;
import org.dalgen.mybatis.ext.tableconfig.TableConfigXmlBuilder;
import org.dalgen.mybatis.generator.GeneratorProperties;


/** The type Auto gen. */
public class AutoGen {
  /** The constant CONFIG_FILE_NAME. */
  public static final String CONFIG_FILE_NAME = "config.xml";

  /** The Dir table configs. */
  public String              dirTableConfigs;

  /** The Dir dal output root. */
  public String              dirDalOutputRoot;

  /** The Table name. */
  public String              tableName;

  /** The Basedir. */
  public String              basedir;

  /** The Dal package name. */
  public String              dalPackageName;

  /** The Dal templates root. */
  public String              dalTemplatesRoot;

  /**
   * Instantiates a new Auto gen.
   *
   * @param tableName the table name
   * @throws IOException the io exception
   */
  public AutoGen(String tableName) throws IOException {
    this.tableName = tableName;

    init();
  }

  private void init() throws IOException {
    Properties props = GeneratorProperties.getProperties();

    props.put("generator_tools_class",
        "org.dalgen.mybatis.util.StringHelper,org.apache.commons.lang3.StringUtils");
    props.put("gg_isOverride", "true");

    props.put("generator_sourceEncoding", "UTF-8");
    props.put("generator_outputEncoding", "UTF-8");
    props.put("tableNameSingularize", "true");

    this.basedir = System.getProperty("user.dir");

    String configFile = MessageFormat.format("{0}/{1}", basedir, CONFIG_FILE_NAME);
    checkConfigFile(configFile);

    GeneratorProperties.load(configFile);
    props.put("genInputCmd", tableName);
    props.put("basedir", basedir);

    this.dirDalOutputRoot = props.getProperty("dir_dal_output_root");
    this.dirTableConfigs = props.getProperty("dir_table_configs");
    this.dalPackageName = props.getProperty("dal_package");
    this.dalTemplatesRoot = props.getProperty("dir_templates_root");
  }

  private void checkConfigFile(String file) {
    File configFile = new File(file);
    if (!configFile.exists()) {
      throw new IllegalArgumentException(
          MessageFormat.format("can't found config file.{0}", configFile.getAbsolutePath()));
    }
  }

  public void dal() throws Exception {
    TableConfigSet tableConfigSet =
        new TableConfigXmlBuilder().parseFromXML(new File(basedir, dirTableConfigs), dalPackageName,
            GenUtils.getTableConfigFiles(new File(basedir, dirTableConfigs)));

    GenUtils.genByTableConfigSet(
        GenUtils.createGeneratorFacade(dirDalOutputRoot,
            resolveTemplate("table_config_set/mybatis"), resolveTemplate("share/dal")),
        tableConfigSet);

    GenUtils.genByTableConfig(
        GenUtils.createGeneratorFacade(dirDalOutputRoot, resolveTemplate("table_config/mybatis"),
            resolveTemplate("share/dal"), resolveTemplate("clazz/paging")),
        tableConfigSet, tableName);

    GenUtils.genByOperation(GenUtils.createGeneratorFacade(dirDalOutputRoot,
        resolveTemplate("operation/dal"), resolveTemplate("share/dal")), tableConfigSet, tableName);
  }

  /**
   * Table.
   *
   * @throws Exception the exception
   */
  public void table() throws Exception {
    this.dirDalOutputRoot = MessageFormat.format("{0}/{1}", basedir, dirTableConfigs);

    GenUtils.genByTable(GenUtils.createGeneratorFacade(dirDalOutputRoot,
        resolveTemplate("table/dalgen_config"), resolveTemplate("share/dal")), tableName);
  }


  private String resolveTemplate(String tpl) {
    return MessageFormat.format("{0}/{1}", dalTemplatesRoot, tpl);
  }
}
