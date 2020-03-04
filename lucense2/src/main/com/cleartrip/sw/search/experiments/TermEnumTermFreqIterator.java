package com.cleartrip.sw.search.experiments;

import java.io.IOException;
import java.util.Comparator;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.spell.TermFreqIterator;
import org.apache.lucene.util.BytesRef;

public class TermEnumTermFreqIterator implements TermFreqIterator {

  private final TermEnum termEnum;
  private Term term = null;
  private float freq = -1f;
  public TermEnumTermFreqIterator(TermEnum termEnum) {
    this.termEnum = termEnum;
  }


  @Override
  public Comparator<BytesRef> getComparator() {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public long weight() {
    // TODO Auto-generated method stub
    return 0;
  }


  @Override
  public BytesRef next() throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

}
