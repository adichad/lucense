package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Scorer;

public class DoubleFieldSource implements DoubleValueSource {
  double[] vals;

  String name;

  public DoubleFieldSource(String name) {
    this.name = name;
  }

  @Override
  public double getValue(Document doc) {
    return Double.parseDouble(doc.get(this.name));
  }

  @Override
  public double getValue(int doc) {
    return this.vals[doc];
  }

  @Override
  public Comparable<?> getComparable(int doc) throws IOException {
    return getValue(doc);
  }

  @Override
  public void setNextReader(IndexReader reader, int docBase) throws IOException {
    this.vals = FieldCache.DEFAULT.getDoubles(reader, this.name);
  }

  @Override
  public void setScorer(Scorer scorer) {}

  @Override
  public String getName() {
    return this.name;
  }

}
