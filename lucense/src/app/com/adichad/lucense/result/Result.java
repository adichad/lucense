/**
 * 
 */
package com.adichad.lucense.result;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author adichad
 * 
 */
public interface Result {
  public void writeTo(OutputStream out) throws IOException;

  public void readFrom(InputStream in) throws IOException;

  @Override
  public String toString();

}
