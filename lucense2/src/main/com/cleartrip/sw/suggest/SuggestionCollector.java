package com.cleartrip.sw.suggest;

import java.io.IOException;

import org.apache.lucene.search.IndexSearcherWrapper;

public abstract class SuggestionCollector {
  public abstract SuggestResult collect(String query,
      IndexSearcherWrapper searcher, int offset, int limit) throws IOException;

}
