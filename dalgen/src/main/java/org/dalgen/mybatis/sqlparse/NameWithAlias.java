package org.dalgen.mybatis.sqlparse;

import org.dalgen.mybatis.util.StringHelper;

public class NameWithAlias {
  private String name;
  private String alias;

  public NameWithAlias(String name, String alias) {
    if (name == null)
      throw new IllegalArgumentException("name must be not null");
    if (name.trim().indexOf(' ') >= 0)
      throw new IllegalArgumentException("error name:" + name);
    if (alias != null && alias.trim().indexOf(' ') >= 0)
      throw new IllegalArgumentException("error alias:" + alias);
    this.name = name.trim();
    this.alias = alias == null ? null : alias.trim();
  }

  public String getName() {
    return name;
  }

  public String getAlias() {
    return StringHelper.isBlank(alias) ? getName() : alias;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    NameWithAlias other = (NameWithAlias) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return StringHelper.isBlank(alias) ? name : name + " as " + alias;
  }
}
