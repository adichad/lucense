package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;

public class StringDocIdSource extends DocIdSource implements StringValueSource {

  public StringDocIdSource() {}

  @Override
  public String getValue(Document doc) {
    return null;
  }

  @Override
  public Comparable<?> getComparable(int doc) throws IOException {
    return getValue(doc);
  }

  @Override
  public String getValue(int doc) {
    return Integer.toString(doc + this.docBase);
  }

}
