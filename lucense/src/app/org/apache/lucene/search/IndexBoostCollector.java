package org.apache.lucene.search;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.IndexReader;

public class IndexBoostCollector extends Collector {

  private final Collector c;

  Map<Integer, Float> indexBoost;

  private float currentReaderBoost;

  private Map<String, Float> readerBoosts;

  public IndexBoostCollector(Collector c, Map<String, Float> readerBoosts) throws IOException {
    this.c = c;
    this.indexBoost = new HashMap<Integer, Float>();
    this.readerBoosts = readerBoosts;
  }

  @Override
  public boolean acceptsDocsOutOfOrder() {
    return this.c.acceptsDocsOutOfOrder();
  }

  @Override
  public void collect(int doc) throws IOException {
    this.c.collect(doc);
    this.indexBoost.put(doc, this.currentReaderBoost);
  }

  @Override
  public void setNextReader(IndexReader reader, int docBase) throws IOException {
    this.c.setNextReader(reader, docBase);
    this.currentReaderBoost = this.readerBoosts.get(reader.directory().toString());
  }

  @Override
  public void setScorer(Scorer scorer) throws IOException {
    this.c.setScorer(scorer);
  }

  public float getIndexBoost(int doc) {

    return this.indexBoost.get(doc);
  }
}
