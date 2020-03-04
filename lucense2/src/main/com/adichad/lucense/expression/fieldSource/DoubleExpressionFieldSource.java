package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.DoubleLucenseExpression;
import com.adichad.lucense.expression.Slates;

public class DoubleExpressionFieldSource extends ExpressionFieldSource implements DoubleValueSource {

  private double lastValue = 0d;
  private final Slates state;
  private final Context cx;

  public DoubleExpressionFieldSource(String name, DoubleLucenseExpression expr, Context cx) {
    super(name, expr);
    this.state = ((DoubleLucenseExpression) this.expr).initState(cx);
    this.cx = cx;
  }

  @Override
  public double getValue(int doc) throws IOException {
    if (doc != this.lastDoc) {
      this.lastValue = ((DoubleLucenseExpression) this.expr).evaluate(doc, state, cx);
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
    return ((DoubleLucenseExpression) this.expr).evaluate(doc, state, cx);
  }

}
