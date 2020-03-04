package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;

import com.adichad.lucense.expression.LucenseExpression;

public abstract class ExpressionFieldSource implements ValueSource {
  protected LucenseExpression expr;

  protected int lastDoc = -1;

  protected String name;

  public ExpressionFieldSource(String name, LucenseExpression expr) {
    this.expr = expr;
    this.name = name;
  }

  @Override
  public void setNextReader(IndexReader reader, int docBase) throws IOException {
    this.expr.setNextReader(reader, docBase);
    this.lastDoc = -1;
  }

  @Override
  public void setScorer(Scorer scorer) {
    this.expr.setScorer(scorer);
    // this.scorer = (CustomScorer)scorer;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public int hashCode() {
    return this.name.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof BooleanExpressionFieldSource)
      return this.name.equals(o);
    return false;
  }

}
