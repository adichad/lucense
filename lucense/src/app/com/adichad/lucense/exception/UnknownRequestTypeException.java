/******************************************************************************
 * UnknownRequestTypeException.java
 * Thrown when configuration could not be loaded
 * ***************************************************************************/

package com.adichad.lucense.exception;

public class UnknownRequestTypeException extends LucenseException {

  /**
	 * 
	 */
  private static final long serialVersionUID = 4497670740834023936L;

  @Override
  public String getMessage() {
    return "Unknown request type";
  }

  @Override
  public Type getType() {
    // TODO Auto-generated method stub
    return Type.UNKNOWNREQUESTTYPE;
  }

}
