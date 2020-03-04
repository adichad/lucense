package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;

import com.adichad.lucense.expression.DoubleLucenseExpression;

public class DoubleExpressionFieldSource extends ExpressionFieldSource implements DoubleValueSource {

  private double lastValue = 0d;

  public DoubleExpressionFieldSource(String name, DoubleLucenseExpression expr) {
    super(name, expr);
  }

  @Override
  public double getValue(int doc) throws IOException {
    if (doc != this.lastDoc) {
      this.lastValue = ((DoubleLucenseExpression) this.expr).evaluate(doc);
      this.lastDoc = doc;
    }

    return this.lastValue;
  }

  @Override
  public Comparable<?> getComparable(int doc) throws IOException {
    return getValue(doc);
  }

  @Override
  public double getValue(Document doc) {
    return ((DoubleLucenseExpression) this.expr).evaluate(doc);
  }

}
