package org.dalgen.mybatis.provider.db.table;

/**
 * public enum ${enumClassName} { ${enumAlias}(${enumKey},${enumDesc}); private String key; private
 * String value; }
 *
 * @author badqiu
 */
public class EnumMetaDada {
  private String enumAlias;
  private String enumKey;
  private String enumDesc;

  public EnumMetaDada(String enumAlias, String enumKey, String enumDesc) {
    super();
    this.enumAlias = enumAlias;
    this.enumKey = enumKey;
    this.enumDesc = enumDesc;
  }

  public String getEnumAlias() {
    return enumAlias;
  }

  public String getEnumKey() {
    return enumKey;
  }

  public String getEnumDesc() {
    return enumDesc;
  }
}
