package org.dalgen.mybatis.typemapping;

import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.TreeSet;

import org.dalgen.mybatis.provider.java.JavaClass;
import org.dalgen.mybatis.util.StringHelper;

public class JavaImport {
  TreeSet<String> imports = new TreeSet<String>();

  public static void addImportClass(Set<JavaClass> set, Class... clazzes) {
    if (clazzes == null)
      return;
    for (Class c : clazzes) {
      if (c == null)
        continue;
      if (c.getName().startsWith("java.lang."))
        continue;
      if (c.isPrimitive())
        continue;
      if ("void".equals(c.getName()))
        continue;
      if (c.isAnonymousClass())
        continue;
      if (!Modifier.isPublic(c.getModifiers()))
        continue;
      if (JavaImport.isNeedImport(c.getName())) {
        set.add(new JavaClass(c));
      }
    }
  }

  public static boolean isNeedImport(String type) {
    if (StringHelper.isBlank(type)) {
      return false;
    }
    if ("void".equals(type)) {
      return false;
    }

    if (type.startsWith("java.lang.")) {
      return false;
    }

    if (JavaPrimitiveTypeMapping.getPrimitiveTypeOrNull(type) != null) {
      return false;
    }

    if ((type.indexOf(".") < 0)
        || Character.isLowerCase(StringHelper.getJavaClassSimpleName(type).charAt(0))) {
      return false;
    }

    return true;
  }

  public void addImport(String javaType) {
    if (isNeedImport(javaType)) {
      imports.add(javaType.replace("$", "."));
    }
  }

  public void addImport(JavaImport javaImport) {
    if (javaImport != null)
      imports.addAll(javaImport.getImports());
  }

  public TreeSet<String> getImports() {
    return imports;
  }
}
