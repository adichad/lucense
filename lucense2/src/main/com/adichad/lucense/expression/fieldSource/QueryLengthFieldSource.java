package com.adichad.lucense.expression.fieldSource;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;

public abstract class QueryLengthFieldSource implements ValueSource {
  String name;

  protected int val;

  public QueryLengthFieldSource(String field, Object2IntOpenHashMap<String> map) {
    this.name = "_qlen_" + field + "_";
    this.val = 0;
    if (field.equals("max")) {
      for (int c : map.values())
        if (this.val < c)
          this.val = c;
    } else {
      this.val = map.containsKey(field) ? map.getInt(field) : 0;
    }
  }

  @Override
  public void setNextReader(IndexReader reader, int docBase) throws IOException {}

  @Override
  public void setScorer(Scorer scorer) {}

  @Override
  public String getName() {
    return this.name;
  }

}
