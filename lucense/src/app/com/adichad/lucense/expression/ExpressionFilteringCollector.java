package com.adichad.lucense.expression;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;

public class ExpressionFilteringCollector extends Collector {

  private final Collector c;

  // private int totalHits;

  private BooleanLucenseExpression expression;

  private Boolean exclude;

  public ExpressionFilteringCollector(Collector c, BooleanLucenseExpression expression2, Boolean exclude)
      throws IOException {
    this.c = c;
    this.expression = expression2;
    this.exclude = exclude;
  }

  @Override
  public boolean acceptsDocsOutOfOrder() {
    return this.c.acceptsDocsOutOfOrder();
  }

  @Override
  public void collect(int doc) throws IOException {
    // ++this.totalHits;
    if (this.expression.evaluate(doc) ^ this.exclude) {
      this.c.collect(doc);
    }
  }

  @Override
  public void setNextReader(IndexReader reader, int docBase) throws IOException {
    this.c.setNextReader(reader, docBase);
    this.expression.setNextReader(reader, docBase);
  }

  @Override
  public void setScorer(Scorer scorer) throws IOException {
    this.c.setScorer(scorer);
    this.expression.setScorer(scorer);
  }

}
