package com.adichad.lucense.grouping;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;

import com.adichad.lucense.result.SearchResult;

public class GroupingCollector extends Collector {

  private final Collector c;

  private List<Grouper> groupers;

  public GroupingCollector(Collector c, List<Grouper> groupers) throws IOException {
    this.c = c;
    this.groupers = groupers;
  }

  @Override
  public boolean acceptsDocsOutOfOrder() {
    return this.c.acceptsDocsOutOfOrder();
  }

  @Override
  public void collect(int doc) throws IOException {
    this.c.collect(doc);
    for (Grouper grouper : this.groupers) {
      grouper.collect(doc);
    }
  }

  @Override
  public void setNextReader(IndexReader reader, int docBase) throws IOException {
    this.c.setNextReader(reader, docBase);
    for (Grouper grouper : this.groupers) {
      grouper.setNextReader(reader, docBase);
    }
  }

  @Override
  public void setScorer(Scorer scorer) throws IOException {
    this.c.setScorer(scorer);
    for (Grouper grouper : this.groupers) {
      grouper.setScorer(scorer);
    }
  }

  public void fill(SearchResult res) {
    for (Grouper grouper : this.groupers) {
      grouper.fillGroupings(res);
    }
  }
}
