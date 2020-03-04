package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;

public class FloatLCSLengthSource extends LCSLengthFieldSource implements FloatValueSource {

  private Set<String> scoreFields;

  public FloatLCSLengthSource(String field, Set<String> scoreFields) {
    super(field);
    this.scoreFields = scoreFields;
  }

  @Override
  public float getValue(Document doc) {
    return 0f;
  }

  @Override
  public Comparable<?> getComparable(int doc) throws IOException {
    return getValue(doc);
  }

  @Override
  public float getValue(int doc) throws IOException {
    float val = 0f;
    Map<String, Integer> nmap = this.scorer.lcsLength(this.scoreFields);
    val = nmap.get(this.getfield);
    return val;
  }

}
