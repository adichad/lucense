package com.adichad.lucense.result;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import scala.Cell;

import com.adichad.lucense.bitmap.BitMapOperationStatus;
import com.adichad.lucense.bitmap.CellResolver;
import com.adichad.lucense.result.Result;

public class UpdateAuxResult implements Result {

  int reqId = -1;

  HashSet<CellResolver> cellFailed = null ; 

  byte commandType = -1;

  public UpdateAuxResult(int id, HashSet<CellResolver> cellFailed, byte commandType) {
    this.reqId = id;
    this.cellFailed = cellFailed;
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
    dos.writeByte(cellFailed.size()) ; 
    for (Iterator<CellResolver> iterator = cellFailed.iterator(); iterator.hasNext();) {
       CellResolver cs = (CellResolver) iterator.next();
       dos.writeInt(cs.getOriginal(0).length()) ; 
       dos.writeBytes(cs.getOriginal(0)) ; 
    }
  }

}
