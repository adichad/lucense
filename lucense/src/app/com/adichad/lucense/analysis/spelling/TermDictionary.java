package com.adichad.lucense.analysis.spelling;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.adichad.lucense.request.Request;

public class TermDictionary {

  private static class Entry {
    Long id, freq;

    public Entry(Long id, Long freq) {
      this.id = id;
      this.freq = freq;
    }

    public Long getID() {
      return this.id;
    }

    public Long getFreq() {
      return this.freq;
    }

    @Override
    public String toString() {
      return this.id.toString() + "," + this.freq.toString();
    }

    public void writeTo(DataOutputStream out) throws IOException {
      out.writeLong(this.id);
      out.writeLong(this.freq);
    }

    public static Entry readFrom(DataInputStream in) throws IOException {
      long id = in.readLong();
      long freq = in.readLong();

      Entry e = new Entry(id, freq);
      return e;
    }
  }

  private HashMap<String, Entry> terms;

  private Long maxID = -1l;

  private Long totalFreq = 0l;

  private HashMap<String, Double> labelMap;

  private boolean isNormalized;

  public TermDictionary() {
    this.terms = new HashMap<String, Entry>();
    this.labelMap = new HashMap<String, Double>();
    this.isNormalized = false;
  }

  public void writeTo(OutputStream out) throws IOException {
    DataOutputStream dout = new DataOutputStream(out);
    dout.writeLong(this.maxID);
    dout.writeLong(this.totalFreq);
    dout.writeInt(this.terms.size());
    for (String term : this.terms.keySet()) {
      dout.writeInt(term.length());
      dout.writeBytes(term);
      this.terms.get(term).writeTo(dout);
    }
  }

  static TermDictionary readFrom(InputStream in) throws IOException {
    DataInputStream din = new DataInputStream(in);
    TermDictionary dict = new TermDictionary();
    dict.maxID = din.readLong();
    dict.totalFreq = din.readLong();
    int len = din.readInt();
    for (int i = 0; i < len; i++) {
      String term = Request.readString(din);
      dict.terms.put(term, Entry.readFrom(din));
    }

    for (String term : dict.terms.keySet()) {
      dict.labelMap.put(term, dict.terms.get(term).freq.doubleValue() / dict.totalFreq.doubleValue());
    }
    dict.isNormalized = true;
    return dict;
  }

  public boolean addTerm(String label) {
    if (!this.isNormalized) {
      if (this.terms.containsKey(label)) {
        this.terms.get(label).freq++;
        this.labelMap.put(label, this.labelMap.get(label) + 1.0d);
        this.totalFreq++;
        return false;
      }
      this.terms.put(label, new Entry(++this.maxID, 1l));
      this.labelMap.put(label, 1.0d);
      this.totalFreq++;
      return true;
    } else
      throw new RuntimeException("dictionary was normalized, sealed");
  }

  public Long getID(String label) {
    if (this.terms.containsKey(label))
      return this.terms.get(label).getID();
    return 0l;
  }

  public Long getFreq(String label) {
    if (this.terms.containsKey(label))
      return this.terms.get(label).getFreq();
    return 0l;
  }

  public Double getProbability(String label) {
    if (this.terms.containsKey(label))
      return this.terms.get(label).getFreq().doubleValue() / this.totalFreq.doubleValue();
    return 0d;
  }

  public void normalize() {
    if (!this.isNormalized) {
      for (Map.Entry<String, Double> entry : this.labelMap.entrySet()) {
        entry.setValue(entry.getValue() / this.totalFreq.doubleValue());
      }
      this.isNormalized = true;
    }
  }

  @Override
  public String toString() {
    StringBuilder buff = new StringBuilder();
    for (String label : this.terms.keySet()) {
      Entry entry = this.terms.get(label);
      buff.append(label + " " + entry.toString() + "\n");
    }

    return buff.toString();
  }

  public Map<String, Double> getEntrySet() {
    return this.labelMap;
  }

}
