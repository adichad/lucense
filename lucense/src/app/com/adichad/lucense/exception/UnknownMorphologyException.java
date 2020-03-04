package com.adichad.lucense.exception;

public class UnknownMorphologyException extends LucenseException {

  /**
     * 
     */
  private static final long serialVersionUID = 1L;

  @Override
  public String getMessage() {
    return "Unknown morphology specified";
  }

  @Override
  public Type getType() {
    // TODO Auto-generated method stub
    return Type.UNKNOWNMORPHOLOGY;
  }

}
