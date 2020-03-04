package com.adichad.lucense.expression;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;

public class IntExpressionCollector extends ExpressionCollector {

  protected IntLucenseExpression expr;

  protected Int2IntArrayMap vals = new Int2IntArrayMap();

  public IntExpressionCollector(IntLucenseExpression expr, Collector c) throws IOException {
    super(c);
    this.expr = expr;
  }

  @Override
  public void collect(int doc) throws IOException {
    ++this.totalHits;
    this.c.collect(doc);
    this.vals.put(doc, this.expr.evaluate(doc));
  }

  @Override
  public void setNextReader(IndexReader reader, int docBase) throws IOException {
    this.c.setNextReader(reader, docBase);
    this.expr.setNextReader(reader, docBase);
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