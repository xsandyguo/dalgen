package org.dalgen.mybatis.plugin;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.dalgen.mybatis.bootstrap.AutoGen;
import org.dalgen.mybatis.bootstrap.ExecuteTarget;
import org.dalgen.mybatis.util.ConfInit;

/**
 * The type Dal gen mojo.
 */
@Mojo(name = "run")
public class DALGenMojo extends AbstractMojo {

  @Parameter(property = "execute.target")
  private String executeTarget;

  @Parameter(property = "table.name")
  private String tableName;

  @Parameter(property = "config.dir")
  private File   configDir;

  @Parameter(defaultValue = "${project.basedir}", readonly = true)
  private File   baseDir;


  @Override
  public void execute() throws MojoExecutionException {
    try {
      run();
      System.out.println(tableName);
      System.out.println(configDir);
      System.out.println(executeTarget);
      System.out.println(baseDir.getAbsolutePath());
    } catch (Exception e) {
      throw new MojoExecutionException(e);
    }
  }

  private void run() throws Exception {
    ExecuteTarget cmd = null;
    try {
      cmd = ExecuteTarget.valueOf(executeTarget);
    } catch (IllegalArgumentException e) {
      printUsage();
    }

    switch (cmd) {
      case help -> printUsage();
      case rt -> this.rt();
      case dal -> this.dal();
      case table -> this.table();
    }

    System.out.println("---------------------Generator executed SUCCESS---------------------");
  }

  private void dal() throws Exception {
    checkArgs();
    new AutoGen(tableName).dal();
  }

  private void table() throws Exception {
    checkArgs();
    new AutoGen(tableName).table();
  }

  private void checkArgs() {
    if (StringUtils.isBlank(tableName)) {
      System.out.println("no table name.");
      printUsage();
    }

    if (configDir == null) {
      System.out.println("no config dir.");
      printUsage();
    }
  }


  private void rt() {
    ConfInit.copyTemplateFile(baseDir.getAbsolutePath() + "/templates");
  }

  private static void printUsage() {
    System.out
        .println("mvn dalgen:run -Dexecute.target=rt|dal|table -Dtable.name= -Dconfig.dir=xxdb");
    System.exit(-1);
  }

}
