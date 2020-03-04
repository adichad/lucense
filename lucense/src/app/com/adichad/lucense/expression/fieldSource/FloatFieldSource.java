package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Scorer;

public class FloatFieldSource implements FloatValueSource {
  float[] vals;

  String name;

  public FloatFieldSource(String name) {
    this.name = name;
  }

  @Override
  public float getValue(Document doc) {
    return Float.parseFloat(doc.get(this.name));
  }

  @Override
  public float getValue(int doc) {
    return this.vals[doc];
  }

  @Override
  public void setNextReader(IndexReader reader, int docBase) throws IOException {
    this.vals = FieldCache.DEFAULT.getFloats(reader, this.name);
  }

  @Override
  public Comparable<?> getComparable(int doc) throws IOException {
    return getValue(doc);
  }

  @Override
  public void setScorer(Scorer scorer) {}

  @Override
  public String getName() {
    return this.name;
  }

}
