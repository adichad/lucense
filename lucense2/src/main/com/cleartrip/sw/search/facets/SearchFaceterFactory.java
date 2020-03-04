package com.cleartrip.sw.search.facets;

import java.util.Map;
import java.util.Properties;

import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Query;

public abstract class SearchFaceterFactory {
  protected final Map<String, ?> params;
  protected final Properties env;
  
  public SearchFaceterFactory(Map<String, ?> params, Properties env) {
    this.params = params;
    this.env = env;
  }
  
  public abstract SearchFaceter createFaceter(Collector c);
  public abstract Query createFilter(String[] vals);
}
