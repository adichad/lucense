package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;

public interface StringValueSource extends ValueSource {
  public String getValue(Document doc);

  public String getValue(int doc) throws IOException;
}
