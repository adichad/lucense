package com.adichad.lucense.bitmap;

public final class BitMapOperationStatus {

  private String msg;

  public static final BitMapOperationStatus SUCCESS = new BitMapOperationStatus("SUCCESS");

  public static final BitMapOperationStatus NOTFOUND = new BitMapOperationStatus("NOTFOUND");

  public static final BitMapOperationStatus FAIL = new BitMapOperationStatus("FAIL");

  public BitMapOperationStatus(String msg) {
    this.msg = msg;
  }

}
