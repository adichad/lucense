/**
 * 
 */
package com.adichad.lucense.result;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author adichad
 * 
 */
public class PingResult implements Result {

  private static final String response = "PONG";

  private int requestID;

  public PingResult(int requestID) {
    this.requestID = requestID;
  }

  /*
   * (non-Javadoc)
   * @see com.adichad.lucense.request.Result#toByteArray()
   */
  @Override
  public void writeTo(OutputStream out) throws IOException {
    DataOutputStream dos = new DataOutputStream(out);
    dos.writeByte(0);
    dos.writeInt(this.requestID);
    dos.writeInt(response.length());
    dos.writeBytes(response);
  }

  @Override
  public void readFrom(InputStream in) throws IOException {}
}
