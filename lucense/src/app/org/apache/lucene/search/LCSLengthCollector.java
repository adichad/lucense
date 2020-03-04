package org.apache.lucene.search;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.IndexReader;

import com.adichad.lucense.expression.fieldSource.ValueSourceFactory;

public class LCSLengthCollector extends Collector {

  private final Collector c;

  private CustomScorer scorer;

  Map<Integer, Map<String, Integer>> lcslength;

  private List<String> fields;

  public LCSLengthCollector(List<String> fields, Collector c) throws IOException {
    this.c = c;
    this.lcslength = new HashMap<Integer, Map<String, Integer>>();
    this.fields = fields;
  }

  @Override
  public boolean acceptsDocsOutOfOrder() {
    return this.c.acceptsDocsOutOfOrder();
  }

  @Override
  public void collect(int doc) throws IOException {
    this.c.collect(doc);
    this.lcslength.put(doc, this.scorer.lcsLength(ValueSourceFactory.scoreFields));
  }

  @Override
  public void setNextReader(IndexReader reader, int docBase) throws IOException {
    this.c.setNextReader(reader, docBase);
  }

  @Override
  public void setScorer(Scorer scorer) throws IOException {
    this.scorer = (CustomScorer) scorer;
    this.c.setScorer(scorer);
  }

  public Map<String, Integer> getLCSLength(int doc) {
    Map<String, Integer> retnumwords = new HashMap<String, Integer>();
    for (String field : this.fields) {
      retnumwords.put(field, this.lcslength.get(doc).get(field));
    }
    return retnumwords;
  }
}
