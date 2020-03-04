package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;

public abstract class ScoreSource implements ValueSource {
  Scorer scorer;

  protected int lastDoc = -1;

  @Override
  public void setNextReader(IndexReader reader, int docBase) throws IOException {}

  @Override
  public void setScorer(Scorer scorer) {
    this.scorer = scorer;
  }

  @Override
  public String getName() {
    return "_score";
  }

}
