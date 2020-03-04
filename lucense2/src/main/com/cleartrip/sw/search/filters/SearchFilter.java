package com.cleartrip.sw.search.filters;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;

public abstract class SearchFilter extends Collector {
  private Collector next;

  public SearchFilter(Collector next) {
    this.next = next;
  }

  public final void collect(int doc) throws IOException {
    process(doc);
    if (next != null)
      next.collect(doc);
  }

  @Override
  public void setNextReader(IndexReader reader, int docBase) throws IOException {
    if (next != null)
      next.setNextReader(reader, docBase);
  }

  @Override
  public void setScorer(Scorer scorer) throws IOException {
    if (next != null)
      next.setScorer(scorer);
  }

  @Override
  public final boolean acceptsDocsOutOfOrder() {
    if (next != null)
      return next.acceptsDocsOutOfOrder();
    return true;
  }

  protected abstract void process(int doc) throws IOException;
  
  public abstract int hiddenCount();
  
  public abstract boolean select(ScoreDoc sd, IndexSearcher searcher) throws Exception;

  public String selectByValue(String value, IndexSearcher searcher) throws IOException {
    return "";
  }

}
