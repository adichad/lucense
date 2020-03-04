package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;

public interface FloatValueSource extends ValueSource {
  public float getValue(Document doc);

  public float getValue(int doc) throws IOException;
}
