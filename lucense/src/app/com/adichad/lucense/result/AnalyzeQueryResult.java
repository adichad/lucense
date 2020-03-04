package com.adichad.lucense.result;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class AnalyzeQueryResult implements Result {

  private Map<String, List<String>> fieldTermMap;

  private int id;

  public AnalyzeQueryResult(int id, Map<String, List<String>> terms) {
    this.id = id;
    this.fieldTermMap = terms;
  }

  @Override
  public void readFrom(InputStream in) throws IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public void writeTo(OutputStream out) throws IOException {
    DataOutputStream dos = new DataOutputStream(out);
    dos.writeByte(13);
    dos.writeInt(this.id);

    dos.writeInt(this.fieldTermMap.size());
    for (String field : this.fieldTermMap.keySet()) {
      dos.writeInt(field.length());
      dos.writeBytes(field);
      List<String> terms = this.fieldTermMap.get(field);
      dos.writeInt(terms.size());
      for (String term : terms) {
        dos.writeInt(term.length());
        dos.writeBytes(term);
      }
    }
  }

}
