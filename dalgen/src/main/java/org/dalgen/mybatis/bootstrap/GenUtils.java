package org.dalgen.mybatis.bootstrap;

import java.io.File;
import java.util.*;

import org.dalgen.mybatis.ext.tableconfig.TableConfig;
import org.dalgen.mybatis.ext.tableconfig.TableConfigSet;
import org.dalgen.mybatis.generator.GeneratorFacade;
import org.dalgen.mybatis.provider.db.sql.Sql;
import org.dalgen.mybatis.util.BeanHelper;

/** The type Gen utils. */
public class GenUtils {

  /**
   * Gen by table config set.
   *
   * @param generatorFacade the generator facade
   * @param tableConfigSet the table config set
   * @throws Exception the exception
   */
  public static void genByTableConfigSet(GeneratorFacade generatorFacade,
      TableConfigSet tableConfigSet) throws Exception {
    Map map = new HashMap();
    map.putAll(BeanHelper.describe(tableConfigSet));
    map.put("tableConfigSet", tableConfigSet);

    TableConfig tableConfig = tableConfigSet.iterator().next();
    String basepackage = tableConfig.getBasepackage();
    map.put("basepackage_dir", basepackage.replace(".", "/"));
    map.put("basepackage", basepackage);
    generatorFacade.generateByMap(map);
  }

  /**
   * Gen by table config.
   *
   * @param generatorFacade the generator facade
   * @param tableConfigSet the table config set
   * @param tableSqlName the table sql name
   * @throws Exception the exception
   */
  public static void genByTableConfig(GeneratorFacade generatorFacade,
      TableConfigSet tableConfigSet, String tableSqlName) throws Exception {

    Collection<TableConfig> tableConfigs = GenUtils.getTableConfigs(tableConfigSet, tableSqlName);
    for (TableConfig tableConfig : tableConfigs) {
      Map map = new HashMap();
      String[] ignoreProperties = {"sqls"};
      map.putAll(BeanHelper.describe(tableConfig, ignoreProperties));
      map.put("tableConfig", tableConfig);
      generatorFacade.generateByMap(map);
    }
  }

  /**
   * Gen by operation.
   *
   * @param generatorFacade the generator facade
   * @param tableConfigSet the table config set
   * @param tableSqlName the table sql name
   * @throws Exception the exception
   */
  public static void genByOperation(GeneratorFacade generatorFacade, TableConfigSet tableConfigSet,
      String tableSqlName) throws Exception {
    Collection<TableConfig> tableConfigs = GenUtils.getTableConfigs(tableConfigSet, tableSqlName);
    for (TableConfig tableConfig : tableConfigs) {
      for (Sql sql : tableConfig.getSqls()) {
        Map operationMap = new HashMap();
        operationMap.putAll(BeanHelper.describe(sql));
        operationMap.put("sql", sql);
        operationMap.put("tableConfig", tableConfig);
        operationMap.put("basepackage", tableConfig.getBasepackage());
        generatorFacade.generateByMap(operationMap);
      }
    }
  }

  /**
   * Gen by table.
   *
   * @param generatorFacade the generator facade
   * @param tableSqlName the table sql name
   * @throws Exception the exception
   */
  public static void genByTable(GeneratorFacade generatorFacade, String tableSqlName)
      throws Exception {
    generatorFacade.generateByTable(tableSqlName);
  }

  /**
   * Gets table config files.
   *
   * @param basedir the basedir
   * @return the table config files
   */
  public static List<String> getTableConfigFiles(File basedir) {
    String[] tableConfigFilesArray = basedir.list();
    List<String> result = new ArrayList<>();
    for (String str : tableConfigFilesArray) {
      if (str.endsWith(".xml")) {
        result.add(str);
      }
    }
    return result;
  }

  /**
   * Gets table configs.
   *
   * @param tableConfigSet the table config set
   * @param tableSqlName the table sql name
   * @return the table configs
   */
  public static Collection<TableConfig> getTableConfigs(TableConfigSet tableConfigSet,
      String tableSqlName) {
    if ("*".equals(tableSqlName)) {
      return tableConfigSet.getTableConfigs();
    } else {
      TableConfig tableConfig = tableConfigSet.getBySqlName(tableSqlName);
      if (tableConfig == null) {
        throw new RuntimeException("根据tableName:${tableSqlName}没有找到相应的配置文件");
      }
      return Arrays.asList(tableConfig);
    }
  }

  /**
   * Create generator facade generator facade.
   *
   * @param outRootDir the out root dir
   * @param templateRootDirs the template root dirs
   * @return the generator facade
   */
  public static GeneratorFacade createGeneratorFacade(String outRootDir,
      String... templateRootDirs) {
    if (templateRootDirs == null) {
      throw new IllegalArgumentException("templateRootDirs must be not null");
    }
    if (outRootDir == null) {
      throw new IllegalArgumentException("outRootDir must be not null");
    }

    GeneratorFacade gf = new GeneratorFacade();
    gf.getGenerator().setTemplateRootDirs(templateRootDirs);
    gf.getGenerator().setOutRootDir(outRootDir);
    return gf;
  }
}
