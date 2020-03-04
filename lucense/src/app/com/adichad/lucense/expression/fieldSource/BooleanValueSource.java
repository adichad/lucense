package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;

public interface BooleanValueSource extends ValueSource {
  public boolean getValue(Document doc);

  public boolean getValue(int doc) throws IOException;
}
