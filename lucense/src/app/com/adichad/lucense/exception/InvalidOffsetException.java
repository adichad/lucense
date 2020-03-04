package com.adichad.lucense.exception;

public class InvalidOffsetException extends LucenseException {
  private static final long serialVersionUID = 1L;

  private int maxmatches;

  private int offset;

  public InvalidOffsetException(int offset, int maxmatches) {
    this.maxmatches = maxmatches;
    this.offset = offset;
  }

  @Override
  public String getMessage() {
    return "Invalid offset: " + this.offset + " (maxmatches was: " + this.maxmatches + ")";
  }

  @Override
  public Type getType() {
    return LucenseException.Type.INVALIDOFFSETEXCEPTION;
  }

}
