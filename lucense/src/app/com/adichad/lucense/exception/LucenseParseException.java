package com.adichad.lucense.exception;

public class LucenseParseException extends LucenseException {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private Throwable exception;

  public LucenseParseException(Throwable e) {
    this.exception = e;
  }

  @Override
  public String getMessage() {
    return this.exception.getMessage();
  }

  @Override
  public Type getType() {
    return LucenseException.Type.PARSEEXCEPTION;
  }

}
