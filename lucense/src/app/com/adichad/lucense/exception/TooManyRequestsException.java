/******************************************************************************
 * TooManyRequestsException.java
 * Thrown when configuration could not be loaded
 * ***************************************************************************/

package com.adichad.lucense.exception;

public class TooManyRequestsException extends LucenseException {

  /**
	 * 
	 */
  private static final long serialVersionUID = 8458665998048027504L;

  @Override
  public String getMessage() {
    return "Too many concurrant requests";
  }

  @Override
  public Type getType() {
    // TODO Auto-generated method stub
    return Type.TOOMANYREQUESTS;
  }

}
