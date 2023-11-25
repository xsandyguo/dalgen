package org.dalgen.mybatis.provider.db.table;

public class NotFoundTableException extends RuntimeException {
  private static final long serialVersionUID = 5976869128012158628L;

  public NotFoundTableException(String message) {
    super(message);
  }
}
