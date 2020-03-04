package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.FloatLucenseExpression;
import com.adichad.lucense.expression.Slates;

public class FloatExpressionFieldSource extends ExpressionFieldSource implements FloatValueSource {

  private float lastValue = 0f;
  private Slates state;
  private Context cx;

  public FloatExpressionFieldSource(String name, FloatLucenseExpression expr, Context cx) {
    super(name, expr);
    this.state = expr.initState(cx);
    this.cx = cx;
  }

  @Override
  public float getValue(int doc) throws IOException {

    if (doc != this.lastDoc) {
      this.lastValue = ((FloatLucenseExpression) this.expr).evaluate(doc, state, cx);
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
    return ((FloatLucenseExpression) this.expr).evaluate(doc, state, cx);
  }

}
