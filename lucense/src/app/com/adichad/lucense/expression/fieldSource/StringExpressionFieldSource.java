package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;

import com.adichad.lucense.expression.StringLucenseExpression;

public class StringExpressionFieldSource extends ExpressionFieldSource implements StringValueSource {

  private String lastValue = null;

  public StringExpressionFieldSource(String name, StringLucenseExpression expr) {
    super(name, expr);
    // TODO Auto-generated constructor stub
  }

  @Override
  public String getValue(int doc) throws IOException {
    if (doc != this.lastDoc) {
      this.lastValue = ((StringLucenseExpression) this.expr).evaluate(doc);
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
    return ((StringLucenseExpression) this.expr).evaluate(doc);
  }

}
