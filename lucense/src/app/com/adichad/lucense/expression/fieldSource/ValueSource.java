package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;

public interface ValueSource {
  // public abstract ExpressionTree<T> getValue(int doc) throws IOException;

  // public abstract ExpressionTree<T> getValue(Document doc);

  public abstract void setNextReader(IndexReader reader, int docBase) throws IOException;

  public abstract void setScorer(Scorer scorer);

  public abstract String getName();

  public abstract Comparable<?> getComparable(int docid) throws IOException;
}
