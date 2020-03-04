package org.apache.lucene.search;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.IndexReader;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class QueryLengthCollector extends Collector {

  private final Collector c;

  private CustomScorer scorer;

  private List<String> fields;

  private Object2IntOpenHashMap<String> qlenMap;

  public QueryLengthCollector(List<String> fields, Object2IntOpenHashMap<String> qlenMap, Collector c)
      throws IOException {
    this.c = c;
    this.fields = fields;
    this.qlenMap = qlenMap;
  }

  @Override
  public boolean acceptsDocsOutOfOrder() {
    return this.c.acceptsDocsOutOfOrder();
  }

  @Override
  public void collect(int doc) throws IOException {
    this.c.collect(doc);
  }

  @Override
  public void setNextReader(IndexReader reader, int docBase) throws IOException {
    this.c.setNextReader(reader, docBase);
  }

  @Override
  public void setScorer(Scorer scorer) throws IOException {
    this.c.setScorer(scorer);
  }

  public Map<String, Integer> getQueryLength(int doc) {
    Map<String, Integer> retnumwords = new HashMap<String, Integer>();
    for (String field : this.fields) {
      retnumwords.put(field, this.qlenMap.get(field));
    }
    return retnumwords;
  }
}
