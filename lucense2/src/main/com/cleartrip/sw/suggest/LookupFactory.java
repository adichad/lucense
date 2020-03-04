package com.cleartrip.sw.suggest;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.suggest.Lookup;

public abstract class LookupFactory {

  public abstract Lookup getLookup(IndexReader reader, String field)
      throws IOException;
}
