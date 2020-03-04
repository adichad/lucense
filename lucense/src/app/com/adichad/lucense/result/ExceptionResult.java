package com.adichad.lucense.result;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.adichad.lucense.exception.LucenseException;

public class ExceptionResult implements Result {

  private String message;

  private byte type;

  private int id;

  public ExceptionResult(LucenseException e, int id) {
    this.type = e.getType().toByte();
    this.message = e.getMessage();
    this.id = id;
  }

  @Override
  public void readFrom(InputStream in) throws IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public void writeTo(OutputStream out) throws IOException {

    DataOutputStream dos = new DataOutputStream(out);

    dos.writeByte(-1);
    dos.writeInt(this.id);
    dos.writeByte(this.type);
    dos.writeInt(this.message.length());
    dos.writeBytes(this.message);
  }
}
