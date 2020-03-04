package com.cleartrip.sw.search.context;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.search.IndexSearcher;

import com.cleartrip.sw.search.schema.Schema;

public class SearcherWarmingStrategy {
  private Schema      schema;
  private Set<String> fieldCacheWarmables;

  public SearcherWarmingStrategy(Schema schema, List<String> fieldCacheWarmables) {
    this.schema = schema;
    this.fieldCacheWarmables = new HashSet<>(fieldCacheWarmables);
  }

  public void warm(IndexSearcher searcher) throws IOException {

    schema.warmFieldCache(searcher.getIndexReader(), fieldCacheWarmables);

  }
}
