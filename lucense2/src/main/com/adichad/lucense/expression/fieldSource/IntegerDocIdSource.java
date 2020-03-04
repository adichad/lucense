package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;

public class IntegerDocIdSource extends DocIdSource implements IntValueSource {

  public IntegerDocIdSource() {}

  @Override
  public int getValue(Document doc) {
    return 0;
  }

  @Override
  public Comparable<?> getComparable(int doc) throws IOException {
    return getValue(doc);
  }

  @Override
  public int getValue(int doc) {
    return doc + this.docBase;
  }

}
