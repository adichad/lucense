package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;

import com.adichad.lucense.expression.BooleanLucenseExpression;

public class BooleanExpressionFieldSource extends ExpressionFieldSource implements BooleanValueSource {

  private boolean lastValue = false;

  public BooleanExpressionFieldSource(String name, BooleanLucenseExpression expr) {
    super(name, expr);
  }

  @Override
  public boolean getValue(int doc) throws IOException {
    if (doc != this.lastDoc) {
      this.lastValue = ((BooleanLucenseExpression) this.expr).evaluate(doc);
      this.lastDoc = doc;
    }
    return this.lastValue;
  }

  @Override
  public Comparable<?> getComparable(int doc) throws IOException {
    return getValue(doc);
  }

  @Override
  public boolean getValue(Document doc) {
    return ((BooleanLucenseExpression) this.expr).evaluate(doc);
  }
}
