package com.adichad.lucense.exception;

public class NoValidIndexesFoundException extends LucenseException {
  /**
     * 
     */
  private static final long serialVersionUID = 8405621272060097386L;

  @Override
  public String getMessage() {
    return "Indexes being served do not match query criteria";
  }

  @Override
  public Type getType() {
    // TODO Auto-generated method stub
    return Type.NOVALIDINDEXFOUND;
  }
}
