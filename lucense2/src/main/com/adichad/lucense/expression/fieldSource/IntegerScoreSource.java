package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;

public class IntegerScoreSource extends ScoreSource implements IntValueSource {

  private int lastValue = -1;

  public IntegerScoreSource() {
    super();
  }

  @Override
  public int getValue(Document doc) {
    return 0;
  }

  @Override
  public Comparable<?> getComparable(int doc) throws IOException {
    return getValue(doc);
  }

  @Override
  public int getValue(int doc) throws IOException {
    if (doc != this.lastDoc) {
      this.lastValue = (int) this.scorer.score();
      this.lastDoc = doc;
    }
    return this.lastValue;
  }

}
