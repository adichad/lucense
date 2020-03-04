package com.adichad.lucense.result;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.adichad.lucense.bitmap.BitMapOperationStatus;
import com.adichad.lucense.request.RequestFactory.RequestType;
import com.adichad.lucense.result.Result;

public class DeleteAuxResult implements Result {

  int reqId = -1;

  BitMapOperationStatus os = null;

  byte commandType = -1;

  public DeleteAuxResult(int id, BitMapOperationStatus os, byte commandType) {
    this.reqId = id;
    this.os = os;
    this.commandType = commandType;
  }

  @Override
  public void readFrom(InputStream in) throws IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public void writeTo(OutputStream out) throws IOException {
    DataOutputStream dos = new DataOutputStream(out);
    dos.writeByte(this.commandType);
    dos.writeInt(this.reqId);
    dos.writeByte(this.os == BitMapOperationStatus.FAIL ? 0 : 1);
  }

}
