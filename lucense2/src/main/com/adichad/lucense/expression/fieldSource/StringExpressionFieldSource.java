package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.StringLucenseExpression;

public class StringExpressionFieldSource extends ExpressionFieldSource implements StringValueSource {

  private String lastValue = null;
  private final Slates state;
  private final Context cx;

  public StringExpressionFieldSource(String name, StringLucenseExpression expr, Context cx) {
    super(name, expr);
    this.state = expr.initState(cx);
    this.cx = cx;
  }

  @Override
  public String getValue(int doc) throws IOException {
    if (doc != this.lastDoc) {
      this.lastValue = ((StringLucenseExpression) this.expr).evaluate(doc, state, cx);
      this.lastDoc = doc;
    }

    return this.lastValue;
  }

  @Override
  public Comparable<?> getComparable(int doc) throws IOException {
    return getValue(doc);
  }

  @Override
  public String getValue(Document doc) {
    return ((StringLucenseExpression) this.expr).evaluate(doc, state, cx);
  }

}
