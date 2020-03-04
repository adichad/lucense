package com.adichad.lucense.exception;

public class UnknownException extends LucenseException {

  private Throwable exception;

  /**
     * 
     */
  private static final long serialVersionUID = 1L;

  public UnknownException(Throwable e) {
    this.exception = e;
  }

  @Override
  public String getMessage() {
    return this.exception.getMessage();
  }

  @Override
  public Type getType() {
    // TODO Auto-generated method stub
    return Type.UNKNOWNEXCEPTION;
  }

}
