package com.adichad.lucense.grouping;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;

import com.adichad.lucense.result.SearchResult;

public abstract class Grouper {

  public Grouper() {
    super();
  }

  public abstract void collect(int doc) throws IOException;

  public abstract void setNextReader(IndexReader reader, int docBase) throws IOException;

  public abstract void setScorer(Scorer scorer);

  public abstract void setBottom(int slot);

  public abstract void fillGroupings(SearchResult res);

}