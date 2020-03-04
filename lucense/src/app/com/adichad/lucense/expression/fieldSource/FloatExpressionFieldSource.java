package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;

import com.adichad.lucense.expression.FloatLucenseExpression;

public class FloatExpressionFieldSource extends ExpressionFieldSource implements FloatValueSource {

  private float lastValue = 0f;

  public FloatExpressionFieldSource(String name, FloatLucenseExpression expr) {
    super(name, expr);
  }

  @Override
  public float getValue(int doc) throws IOException {

    if (doc != this.lastDoc) {
      this.lastValue = ((FloatLucenseExpression) this.expr).evaluate(doc);
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
    return ((FloatLucenseExpression) this.expr).evaluate(doc);
  }

}
