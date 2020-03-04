package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;

public interface IntValueSource extends ValueSource {
  public int getValue(Document doc);

  public int getValue(int doc) throws IOException;
}
