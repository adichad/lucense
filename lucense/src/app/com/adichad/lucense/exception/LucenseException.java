package com.adichad.lucense.exception;

public abstract class LucenseException extends Exception {

  public enum Type {
    TOOMANYREQUESTS,
    NOVALIDINDEXFOUND,
    UNKNOWNREQUESTTYPE,
    UNKNOWNMORPHOLOGY,
    UNKNOWNEXCEPTION,
    PARSEEXCEPTION,
    INVALIDOFFSETEXCEPTION;

    public byte toByte() {
      switch (this) {
      case TOOMANYREQUESTS:
        return 1;
      case NOVALIDINDEXFOUND:
        return 2;
      case UNKNOWNREQUESTTYPE:
        return 3;
      case UNKNOWNMORPHOLOGY:
        return 4;
      case PARSEEXCEPTION:
        return 5;
      case INVALIDOFFSETEXCEPTION:
        return 6;
      case UNKNOWNEXCEPTION:
        return -1;
      default:
        return 0;
      }

    }
  }

  /**
     * 
     */
  private static final long serialVersionUID = 1L;

  public abstract Type getType();

}
