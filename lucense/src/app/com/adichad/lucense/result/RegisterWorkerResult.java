/**
 * 
 */
package com.adichad.lucense.result;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author adichad
 * 
 */
public class RegisterWorkerResult implements Result {
  private int requestID;

  protected boolean response;

  public RegisterWorkerResult(int requestID) {
    this.requestID = requestID;
  }

  public void setResponse(boolean b) {
    this.response = b;
  }

  /*
   * (non-Javadoc)
   * @see com.adichad.lucense.request.Result#writeTo(java.io.OutputStream)
   */
  @Override
  public void writeTo(OutputStream out) throws IOException {
    DataOutputStream dos = new DataOutputStream(out);
    dos.writeByte(3);
    dos.writeInt(this.requestID);
    dos.writeBoolean(this.response);
  }

  @Override
  public void readFrom(InputStream in) throws IOException {
    DataInputStream dis = new DataInputStream(in);
    dis.readByte();
    this.requestID = dis.readInt();
    this.response = dis.readBoolean();
  }

}
