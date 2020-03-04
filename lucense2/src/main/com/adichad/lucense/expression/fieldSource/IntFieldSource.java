package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Scorer;

public class IntFieldSource implements IntValueSource {
  int[] vals;

  String name;

  public IntFieldSource(String name) {
    this.name = name;
  }

  @Override
  public int getValue(Document doc) {
    return Integer.parseInt(doc.get(this.name));
  }

  @Override
  public Comparable<?> getComparable(int doc) throws IOException {
    return getValue(doc);
  }

  @Override
  public int getValue(int doc) {
    return this.vals[doc];
  }

  @Override
  public void setNextReader(IndexReader reader, int docBase) throws IOException {
    this.vals = FieldCache.DEFAULT.getInts(reader, this.name);
  }

  @Override
  public void setScorer(Scorer scorer) {}

  @Override
  public String getName() {
    return this.name;
  }

}
