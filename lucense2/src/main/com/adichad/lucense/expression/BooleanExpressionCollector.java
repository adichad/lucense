package com.adichad.lucense.expression;

import it.unimi.dsi.fastutil.ints.Int2BooleanArrayMap;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;
import org.mozilla.javascript.Context;

public class BooleanExpressionCollector extends ExpressionCollector {

  protected BooleanLucenseExpression expr;

  protected Int2BooleanArrayMap vals = new Int2BooleanArrayMap();

  private final Slates state;

  private final Context cx;

  private int docBase;

  public BooleanExpressionCollector(BooleanLucenseExpression expr, Collector c, Context cx) throws IOException {
    super(c);
    this.expr = expr;
    this.state = expr.initState(cx);
    this.cx = cx;
  }

  @Override
  public void collect(int doc) throws IOException {
    ++this.totalHits;
    this.c.collect(doc);
    this.vals.put(docBase+doc, this.expr.evaluate(doc, state, cx));
  }

  @Override
  public void setNextReader(IndexReader reader, int docBase) throws IOException {
    this.c.setNextReader(reader, docBase);
    this.expr.setNextReader(reader, docBase);
    this.docBase = docBase;
  }

  @Override
  public void setScorer(Scorer scorer) throws IOException {
    this.c.setScorer(scorer);
    this.expr.setScorer(scorer);
  }

  @Override
  public Object getVal(int doc) {
    return this.vals.get(doc);
  }

}