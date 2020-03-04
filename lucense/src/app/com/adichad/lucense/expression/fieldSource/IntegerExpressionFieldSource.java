package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;

import com.adichad.lucense.expression.IntLucenseExpression;

public class IntegerExpressionFieldSource extends ExpressionFieldSource implements IntValueSource {

  private int lastValue = 0;

  public IntegerExpressionFieldSource(String name, IntLucenseExpression expr) {
    super(name, expr);
  }

  @Override
  public int getValue(int doc) throws IOException {
    if (doc != this.lastDoc) {
      this.lastValue = ((IntLucenseExpression) this.expr).evaluate(doc);
      this.lastDoc = doc;
    }

    return this.lastValue;
  }

  @Override
  public Comparable<?> getComparable(int doc) throws IOException {
    return getValue(doc);
  }

  @Override
  public int getValue(Document doc) {
    return ((IntLucenseExpression) this.expr).evaluate(doc);
  }

}
