package com.adichad.lucense.result;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

public class PluralStemInversionResult implements Result {

  private Map<String, Map<String, Set<String>>> highlightables;

  private int id;

  public PluralStemInversionResult(int id, Map<String, Map<String, Set<String>>> inversions) {
    this.id = id;
    this.highlightables = inversions;
  }

  @Override
  public void readFrom(InputStream in) throws IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public void writeTo(OutputStream out) throws IOException {
    DataOutputStream dos = new DataOutputStream(out);
    dos.writeByte(8);
    dos.writeInt(this.id);

    dos.writeInt(this.highlightables.size());
    for (String field : this.highlightables.keySet()) {
      dos.writeInt(field.length());
      dos.writeBytes(field);
      Map<String, Set<String>> invMap = this.highlightables.get(field);
      dos.writeInt(invMap.size());
      for (String invOrig : invMap.keySet()) {
        dos.writeInt(invOrig.length());
        dos.writeBytes(invOrig);
        Set<String> invs = invMap.get(invOrig);
        dos.writeInt(invs.size());
        for (String inv : invs) {
          dos.writeInt(inv.length());
          dos.writeBytes(inv);
        }
      }
    }
  }

}
