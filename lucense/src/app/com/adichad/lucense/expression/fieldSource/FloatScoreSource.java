package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;

public class FloatScoreSource extends ScoreSource implements FloatValueSource {

  float lastValue = -1f;

  public FloatScoreSource() {
    super();
  }

  @Override
  public float getValue(int doc) throws IOException {
    if (doc != this.lastDoc) {
      this.lastValue = this.scorer.score();
      this.lastDoc = doc;
    }
    return this.lastValue;
  }

  @Override
  public Comparable<?> getComparable(int doc) throws IOException {
    return getValue(doc);
  }

  @Override
  public float getValue(Document doc) {
    return 0f;
  }

}
