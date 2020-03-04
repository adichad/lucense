package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.BooleanLucenseExpression;
import com.adichad.lucense.expression.Slates;

public class BooleanExpressionFieldSource extends ExpressionFieldSource implements BooleanValueSource {

  private boolean lastValue = false;
  private final Slates state;
  private final Context cx;

  public BooleanExpressionFieldSource(String name, BooleanLucenseExpression expr, Context cx) {
    super(name, expr);
    this.state = ((BooleanLucenseExpression) this.expr).initState(cx);
    this.cx = cx;
  }

  @Override
  public boolean getValue(int doc) throws IOException {
    if (doc != this.lastDoc) {
      this.lastValue = ((BooleanLucenseExpression) this.expr).evaluate(doc, state, cx);
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
    return ((BooleanLucenseExpression) this.expr).evaluate(doc, state, cx);
  }
}
