package com.adichad.lucense.result;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.adichad.lucense.result.Result;

public class ReloadConfigResult implements Result {

  int reqId;

  public ReloadConfigResult(int id) {
    this.reqId = id;
  }

  @Override
  public void readFrom(InputStream in) throws IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public void writeTo(OutputStream out) throws IOException {
    DataOutputStream dos = new DataOutputStream(out);
    dos.writeByte(18);
    dos.writeInt(this.reqId);
    
  }

}
