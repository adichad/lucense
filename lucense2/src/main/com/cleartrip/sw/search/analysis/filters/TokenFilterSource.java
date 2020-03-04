package com.cleartrip.sw.search.analysis.filters;

import java.util.Map;
import java.util.Properties;

import org.apache.lucene.analysis.TokenStream;

public abstract class TokenFilterSource {
  public TokenFilterSource(Map<String, ?> params, Properties env) throws Exception {
    
  }
  public abstract TokenStream getTokenStream(TokenStream ts) throws Exception;
}
