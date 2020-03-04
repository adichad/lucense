package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Scorer;

public class BooleanFieldSource implements BooleanValueSource {
  int[] vals;

  String name;

  public BooleanFieldSource(String name) {
    this.name = name;
  }

  @Override
  public boolean getValue(Document doc) {
    return Boolean.parseBoolean(doc.get(this.name));
  }

  @Override
  public boolean getValue(int doc) {
    return this.vals[doc] != 0;
  }

  @Override
  public Comparable<?> getComparable(int doc) throws IOException {
    return getValue(doc);
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
