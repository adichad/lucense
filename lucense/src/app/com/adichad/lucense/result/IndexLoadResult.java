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
public class IndexLoadResult implements Result {
  private int requestID;

  private Boolean loadResultFlag;

  public IndexLoadResult(int requestID, Boolean loadResultFlag) {
    this.requestID = requestID;
    this.loadResultFlag = loadResultFlag;
  }

  /*
   * (non-Javadoc)
   * @see com.adichad.lucense.request.Result#writeTo(java.io.OutputStream)
   */
  @Override
  public void writeTo(OutputStream out) throws IOException {
    DataOutputStream dos = new DataOutputStream(out);
    dos.writeByte(4);
    dos.writeInt(this.requestID);
    dos.writeByte(this.loadResultFlag ? 1 : 0);
  }

  @Override
  public void readFrom(InputStream in) throws IOException {

  }

}
