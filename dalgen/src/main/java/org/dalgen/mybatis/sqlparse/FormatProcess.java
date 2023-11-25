package org.dalgen.mybatis.sqlparse;

import java.util.LinkedList;
import java.util.StringTokenizer;

class FormatProcess {
  boolean            beginLine                  = true;
  boolean            afterBeginBeforeEnd        = false;
  boolean            afterByOrSetOrFromOrSelect = false;
  boolean            afterValues                = false;
  boolean            afterOn                    = false;
  boolean            afterBetween               = false;
  boolean            afterInsert                = false;
  int                inFunction                 = 0;
  int                parensSinceSelect          = 0;
  int                indent                     = 1;
  StringBuffer       result                     = new StringBuffer();
  StringTokenizer    tokens;
  String             lastToken;
  String             token;
  String             lcToken;
  private LinkedList parenCounts                = new LinkedList();
  private LinkedList afterByOrFromOrSelects     = new LinkedList();

  public FormatProcess(String sql) {
    tokens = new StringTokenizer(sql, "()+*/-=<>'`\"[]," + BasicSqlFormatter.WHITESPACE, true);
  }

  private static boolean isFunctionName(String tok) {
    final char begin = tok.charAt(0);
    final boolean isIdentifier = Character.isJavaIdentifierStart(begin) || '"' == begin;
    return isIdentifier && !BasicSqlFormatter.LOGICAL.contains(tok)
        && !BasicSqlFormatter.END_CLAUSES.contains(tok)
        && !BasicSqlFormatter.QUANTIFIERS.contains(tok) && !BasicSqlFormatter.DML.contains(tok)
        && !BasicSqlFormatter.MISC.contains(tok);
  }

  private static boolean isWhitespace(String token) {
    return BasicSqlFormatter.WHITESPACE.indexOf(token) >= 0;
  }

  public String perform() {

    result.append(BasicSqlFormatter.initial);

    while (tokens.hasMoreTokens()) {
      token = tokens.nextToken();
      lcToken = token.toLowerCase();

      if ("'".equals(token)) {
        String t;
        do {
          t = tokens.nextToken();
          token += t;
        } while (!"'".equals(t) && tokens.hasMoreTokens()); // cannot handle single quotes
      } else if ("\"".equals(token)) {
        String t;
        do {
          t = tokens.nextToken();
          token += t;
        } while (!"\"".equals(t));
      }

      if (afterByOrSetOrFromOrSelect && ",".equals(token)) {
        commaAfterByOrFromOrSelect();
      } else if (afterOn && ",".equals(token)) {
        commaAfterOn();
      } else if ("(".equals(token)) {
        openParen();
      } else if (")".equals(token)) {
        closeParen();
      } else if (BasicSqlFormatter.BEGIN_CLAUSES.contains(lcToken)) {
        beginNewClause();
      } else if (BasicSqlFormatter.END_CLAUSES.contains(lcToken)) {
        endNewClause();
      } else if ("select".equals(lcToken)) {
        select();
      } else if (BasicSqlFormatter.DML.contains(lcToken)) {
        updateOrInsertOrDelete();
      } else if ("values".equals(lcToken)) {
        values();
      } else if ("on".equals(lcToken)) {
        on();
      } else if (afterBetween && lcToken.equals("and")) {
        misc();
        afterBetween = false;
      } else if (BasicSqlFormatter.LOGICAL.contains(lcToken)) {
        logical();
      } else if (isWhitespace(token)) {
        white();
      } else {
        misc();
      }

      if (!isWhitespace(token)) {
        lastToken = lcToken;
      }
    }
    return result.toString();
  }

  private void commaAfterOn() {
    out();
    indent--;
    newline();
    afterOn = false;
    afterByOrSetOrFromOrSelect = true;
  }

  private void commaAfterByOrFromOrSelect() {
    out();
    // newline();
  }

  private void logical() {
    if ("end".equals(lcToken)) {
      indent--;
    }
    newline();
    out();
    beginLine = false;
  }

  private void on() {
    indent++;
    afterOn = true;
    newline();
    out();
    beginLine = false;
  }

  private void misc() {
    out();
    if ("between".equals(lcToken)) {
      afterBetween = true;
    }
    if (afterInsert) {
      newline();
      afterInsert = false;
    } else {
      beginLine = false;
      if ("case".equals(lcToken)) {
        indent++;
      }
    }
  }

  private void white() {
    if (!beginLine) {
      result.append(" ");
    }
  }

  private void updateOrInsertOrDelete() {
    out();
    indent++;
    beginLine = false;
    if ("update".equals(lcToken)) {
      newline();
    }
    if ("insert".equals(lcToken)) {
      afterInsert = true;
    }
  }

  private void select() {
    out();
    indent++;
    newline();
    parenCounts.addLast(Integer.valueOf(parensSinceSelect));
    afterByOrFromOrSelects.addLast(Boolean.valueOf(afterByOrSetOrFromOrSelect));
    parensSinceSelect = 0;
    afterByOrSetOrFromOrSelect = true;
  }

  private void out() {
    result.append(token);
  }

  private void endNewClause() {
    if (!afterBeginBeforeEnd) {
      indent--;
      if (afterOn) {
        indent--;
        afterOn = false;
      }
      newline();
    }
    out();
    if (!"union".equals(lcToken)) {
      indent++;
    }
    newline();
    afterBeginBeforeEnd = false;
    afterByOrSetOrFromOrSelect =
        "by".equals(lcToken) || "set".equals(lcToken) || "from".equals(lcToken);
  }

  private void beginNewClause() {
    if (!afterBeginBeforeEnd) {
      if (afterOn) {
        indent--;
        afterOn = false;
      }
      indent--;
      newline();
    }
    out();
    beginLine = false;
    afterBeginBeforeEnd = true;
  }

  private void values() {
    indent--;
    newline();
    out();
    indent++;
    newline();
    afterValues = true;
  }

  private void closeParen() {
    parensSinceSelect--;
    if (parensSinceSelect < 0) {
      indent--;
      parensSinceSelect = ((Integer) parenCounts.removeLast()).intValue();
      afterByOrSetOrFromOrSelect = ((Boolean) afterByOrFromOrSelects.removeLast()).booleanValue();
    }
    if (inFunction > 0) {
      inFunction--;
      out();
    } else {
      if (!afterByOrSetOrFromOrSelect) {
        indent--;
        newline();
      }
      out();
    }
    beginLine = false;
  }

  private void openParen() {
    if (isFunctionName(lastToken) || inFunction > 0) {
      inFunction++;
    }
    beginLine = false;
    if (inFunction > 0) {
      out();
    } else {
      out();
      if (!afterByOrSetOrFromOrSelect) {
        indent++;
        newline();
        beginLine = true;
      }
    }
    parensSinceSelect++;
  }

  private void newline() {
    result.append("\n");
    for (int i = 0; i < indent; i++) {
      result.append(BasicSqlFormatter.indentString);
    }
    beginLine = true;
  }
}
