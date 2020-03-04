package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Scorer;

public class StringFieldSource implements StringValueSource {
  String[] vals;

  String name;

  public StringFieldSource(String name) {
    this.name = name;
  }

  @Override
  public String getValue(Document doc) {
    return doc.get(this.name);
  }

  @Override
  public String getValue(int doc) {
    return this.vals[doc];
  }

  @Override
  public Comparable<?> getComparable(int doc) throws IOException {
    return getValue(doc);
  }

  @Override
  public void setNextReader(IndexReader reader, int docBase) throws IOException {
    this.vals = FieldCache.DEFAULT.getStrings(reader, this.name);
  }

  @Override
  public void setScorer(Scorer scorer) {}

  @Override
  public String getName() {
    return this.name;
  }

}
