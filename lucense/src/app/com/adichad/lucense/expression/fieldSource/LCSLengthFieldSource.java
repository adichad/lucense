package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.CustomScorer;
import org.apache.lucene.search.Scorer;

public abstract class LCSLengthFieldSource implements ValueSource {
  CustomScorer scorer;

  String field;

  protected int lastDoc = -1;

  protected String getfield;

  private String name;

  public LCSLengthFieldSource(String field) {
    this.field = field;
    if (field.equals("max"))
      this.getfield = "@max";
    else
      this.getfield = field;
    this.name = "_lcslen_" + field + "_";
  }

  @Override
  public void setNextReader(IndexReader reader, int docBase) throws IOException {}

  @Override
  public void setScorer(Scorer scorer) {
    this.scorer = (CustomScorer) scorer;
  }

  @Override
  public String getName() {
    return this.name;
  }

}
