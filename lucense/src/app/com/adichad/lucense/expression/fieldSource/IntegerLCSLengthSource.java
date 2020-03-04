package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;

public class IntegerLCSLengthSource extends LCSLengthFieldSource implements IntValueSource {

  private Set<String> scoreFields;

  public IntegerLCSLengthSource(String field, Set<String> scoreFields) {
    super(field);
    this.scoreFields = scoreFields;
  }

  @Override
  public int getValue(Document doc) {
    return 0;
  }

  @Override
  public Comparable<?> getComparable(int doc) throws IOException {
    return getValue(doc);
  }

  @Override
  public int getValue(int doc) throws IOException {
    int val = 0;
    Map<String, Integer> nmap = this.scorer.lcsLength(this.scoreFields);
    val = nmap.get(this.getfield);
    return val;
  }

}
