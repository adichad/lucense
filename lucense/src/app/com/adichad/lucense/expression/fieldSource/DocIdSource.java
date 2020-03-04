package com.adichad.lucense.expression.fieldSource;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;

public abstract class DocIdSource implements ValueSource {
  protected int docBase;

  @Override
  public void setNextReader(IndexReader reader, int docBase) {
    this.docBase = docBase;
  }

  @Override
  public void setScorer(Scorer scorer) {}

  @Override
  public String getName() {
    return "_docid";

  }

}
