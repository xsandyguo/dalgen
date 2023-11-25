/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2008, Red Hat Middleware LLC or third-party contributors as indicated by
 * the @author tags or express copyright attribution statements applied by the authors. All
 * third-party contributions are distributed under license by Red Hat Middleware LLC.
 *
 * This copyrighted material is made available to anyone wishing to use, modify, copy, or
 * redistribute it subject to the terms and conditions of the GNU Lesser General Public License, as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * distribution; if not, write to: Free Software Foundation, Inc. 51 Franklin Street, Fifth Floor
 * Boston, MA 02110-1301 USA
 *
 */
package org.dalgen.mybatis.sqlparse;

import java.util.HashSet;
import java.util.Set;

/**
 * Performs formatting of basic SQL statements (DML + query).
 *
 * @author Gavin King
 * @author Steve Ebersole
 * @author badqiu modify commaAfterByOrFromOrSelect() comments newline()
 */
public class BasicSqlFormatter {

  public static final String WHITESPACE    = " \n\r\f\t";
  public static final Set    BEGIN_CLAUSES = new HashSet();
  public static final Set    END_CLAUSES   = new HashSet();
  public static final Set    LOGICAL       = new HashSet();
  public static final Set    QUANTIFIERS   = new HashSet();
  public static final Set    DML           = new HashSet();
  public static final Set    MISC          = new HashSet();
  static final String        indentString  = "    ";
  static final String        initial       = "\n    ";

  static {
    BEGIN_CLAUSES.add("left");
    BEGIN_CLAUSES.add("right");
    BEGIN_CLAUSES.add("inner");
    BEGIN_CLAUSES.add("outer");
    BEGIN_CLAUSES.add("group");
    BEGIN_CLAUSES.add("order");

    END_CLAUSES.add("where");
    END_CLAUSES.add("set");
    END_CLAUSES.add("having");
    END_CLAUSES.add("join");
    END_CLAUSES.add("from");
    END_CLAUSES.add("by");
    END_CLAUSES.add("join");
    END_CLAUSES.add("into");
    END_CLAUSES.add("union");

    LOGICAL.add("and");
    LOGICAL.add("or");
    LOGICAL.add("when");
    LOGICAL.add("else");
    LOGICAL.add("end");

    QUANTIFIERS.add("in");
    QUANTIFIERS.add("all");
    QUANTIFIERS.add("exists");
    QUANTIFIERS.add("some");
    QUANTIFIERS.add("any");

    DML.add("insert");
    DML.add("update");
    DML.add("delete");

    MISC.add("select");
    MISC.add("on");
  }

  public String format(String source) {
    return new FormatProcess(source).perform();
  }

}
