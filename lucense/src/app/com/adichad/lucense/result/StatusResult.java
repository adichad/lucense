/**
 * 
 */
package com.adichad.lucense.result;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.adichad.lucense.searchd.ServerStatus;

/**
 * @author adichad
 * 
 */
public class StatusResult implements Result {

  protected ServerStatus serverStatus;

  private int requestID;

  public StatusResult(int requestID) {
    this.requestID = requestID;
  }

  public void setStatus(ServerStatus serverStatus) {
    this.serverStatus = serverStatus;
  }

  /*
   * (non-Javadoc)
   * @see com.adichad.lucense.request.Result#writeTo(java.io.OutputStream)
   */
  @Override
  public void writeTo(OutputStream out) throws IOException {
    DataOutputStream dos = new DataOutputStream(out);
    dos.writeByte(1);
    dos.writeInt(this.requestID);
    dos.writeInt(this.serverStatus.toString().length());
    dos.writeBytes(this.serverStatus.toString());
  }

  @Override
  public void readFrom(InputStream in) throws IOException {

  }
}
