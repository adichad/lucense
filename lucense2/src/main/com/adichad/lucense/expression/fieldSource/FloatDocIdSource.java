package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;

public class FloatDocIdSource extends DocIdSource implements FloatValueSource {

  public FloatDocIdSource() {}

  @Override
  public float getValue(Document doc) {
    return 0f;
  }

  @Override
  public float getValue(int doc) {
    return doc + this.docBase;
  }

  @Override
  public Comparable<?> getComparable(int doc) throws IOException {
    return getValue(doc);
  }

  @Override
  public String getName() {
    return "_docid";
  }

}
