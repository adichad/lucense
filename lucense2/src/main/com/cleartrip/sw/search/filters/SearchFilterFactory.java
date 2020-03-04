package com.cleartrip.sw.search.filters;

import java.util.Map;
import java.util.Properties;

import org.apache.lucene.search.Collector;

public abstract class SearchFilterFactory {
  protected final Map<String, ?> params;
  protected final Properties env;
  
  public SearchFilterFactory(Map<String, ?> params, Properties env) {
    this.params = params;
    this.env = env;
  }
  public abstract SearchFilter createFilter(Collector c);
}
