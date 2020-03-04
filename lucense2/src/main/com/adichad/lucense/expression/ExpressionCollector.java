package com.adichad.lucense.expression;

import java.io.IOException;

import org.apache.lucene.search.Collector;

public abstract class ExpressionCollector extends Collector {

  protected final Collector c;

  protected int totalHits;

  // Map<Integer, Object> vals;
  // private LucenseExpression expr;

  public ExpressionCollector(Collector c) throws IOException {
    this.c = c;
    this.totalHits = 0;
    // vals = new HashMap<Integer, Object>();
    // this.expr = expr;
  }

  @Override
  public boolean acceptsDocsOutOfOrder() {
    return this.c.acceptsDocsOutOfOrder();
  }

  public abstract Object getVal(int doc);
}