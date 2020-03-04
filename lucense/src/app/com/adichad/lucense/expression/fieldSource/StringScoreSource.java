package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;

public class StringScoreSource extends ScoreSource implements StringValueSource {
  String lastValue = null;

  public StringScoreSource() {}

  @Override
  public String getValue(int doc) throws IOException {
    if (doc != this.lastDoc) {
      this.lastValue = Float.toString(this.scorer.score());
      this.lastDoc = doc;
    }
    return this.lastValue;
  }

  @Override
  public Comparable<?> getComparable(int doc) throws IOException {
    return getValue(doc);
  }

  @Override
  public String getValue(Document doc) {
    return null;
  }

}
