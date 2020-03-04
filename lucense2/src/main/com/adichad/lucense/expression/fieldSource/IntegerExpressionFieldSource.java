package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.IntLucenseExpression;
import com.adichad.lucense.expression.Slates;

public class IntegerExpressionFieldSource extends ExpressionFieldSource implements IntValueSource {

  private int lastValue = 0;
  private final Slates state;
  private final Context cx;

  public IntegerExpressionFieldSource(String name, IntLucenseExpression expr, Context cx) {
    super(name, expr);
    this.state = expr.initState(cx);
    this.cx = cx;
  }

  @Override
  public int getValue(int doc) throws IOException {
    if (doc != this.lastDoc) {
      this.lastValue = ((IntLucenseExpression) this.expr).evaluate(doc, state, cx);
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
    return ((IntLucenseExpression) this.expr).evaluate(doc, state, cx);
  }

}
