/**
 * 
 */
package com.adichad.lucense.result;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author aditya
 */
public class DeleteDocumentsResult implements Result {
  private int requestID;

  public DeleteDocumentsResult(int requestID) {
    this.requestID = requestID;
    ;
  }

  /*
   * (non-Javadoc)
   * @see com.adichad.lucense.request.Result#writeTo(java.io.OutputStream)
   */
  @Override
  public void writeTo(OutputStream out) throws IOException {
    DataOutputStream dos = new DataOutputStream(out);
    dos.writeByte(12);
    dos.writeInt(this.requestID);
  }

  @Override
  public void readFrom(InputStream in) throws IOException {

  }

}
