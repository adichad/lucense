package com.adichad.lucense.result;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;

import org.apache.lucene.index.Term;

public class TermStatsResult implements Result {

  private LinkedHashMap<String, LinkedHashMap<String, Integer>> terms;

  private int id;

  public TermStatsResult(int id, int limit) {
    this.id = id;
    this.terms = new LinkedHashMap<String, LinkedHashMap<String, Integer>>(limit);
  }

  @Override
  public void readFrom(InputStream in) throws IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public void writeTo(OutputStream out) throws IOException {
    DataOutputStream dos = new DataOutputStream(out);
    dos.writeByte(14);
    dos.writeInt(this.id);

    dos.writeInt(this.terms.size());
    for (String field : terms.keySet()) {
      dos.writeInt(field.length());
      dos.writeBytes(field);
      LinkedHashMap<String, Integer> texts = terms.get(field);
      dos.writeInt(texts.size());
      for (String text : texts.keySet()) {
        dos.writeInt(text.length());
        dos.writeBytes(text);
        dos.writeInt(texts.get(text));
      }

    }
  }

  public void addTerm(Term term, int docFreq) {
    if (!terms.containsKey(term.field())) {
      terms.put(term.field(), new LinkedHashMap<String, Integer>());
    }
    this.terms.get(term.field()).put(term.text(), docFreq);

  }

}
