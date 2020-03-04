package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;

public interface DoubleValueSource extends ValueSource {
  public double getValue(Document doc);

  public double getValue(int doc) throws IOException;
}
