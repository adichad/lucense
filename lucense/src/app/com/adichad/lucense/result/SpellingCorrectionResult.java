package com.adichad.lucense.result;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import com.adichad.lucense.analysis.spelling.SpellingCorrectionContext.CorrectionContextEntry;

public class SpellingCorrectionResult implements Result {

  private Map<CorrectionContextEntry, Map<String, Double>> corrections;

  private int id;

  public SpellingCorrectionResult(int id, Map<CorrectionContextEntry, Map<String, Double>> corrections) {
    this.id = id;
    this.corrections = corrections;
  }

  @Override
  public void readFrom(InputStream in) throws IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public void writeTo(OutputStream out) throws IOException {
    DataOutputStream dos = new DataOutputStream(out);
    dos.writeByte(7);
    dos.writeInt(this.id);

    dos.writeInt(this.corrections.size());
    for (CorrectionContextEntry e : this.corrections.keySet()) {
      dos.writeInt(e.getTerm().length());
      dos.writeBytes(e.getTerm());
      dos.writeInt(e.getPos());
      Map<String, Double> innerMap = this.corrections.get(e);
      dos.writeInt(innerMap.size());
      for (String corr : innerMap.keySet()) {
        dos.writeInt(corr.length());
        dos.writeBytes(corr);
        String str = innerMap.get(corr).toString();
        dos.writeInt(str.length());
        dos.writeBytes(str);
      }
    }

  }

}
