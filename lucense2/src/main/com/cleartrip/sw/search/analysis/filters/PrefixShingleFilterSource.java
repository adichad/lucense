package com.cleartrip.sw.search.analysis.filters;

import java.util.Map;
import java.util.Properties;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

public class PrefixShingleFilterSource extends TokenFilterSource {

  private final int maxShingleSize;

  public PrefixShingleFilterSource(Map<String, ?> params, Properties env) throws Exception {
    super(params, env);
    int maxShingleSize = (Integer)params.get("maxShingleSize");
    if (maxShingleSize < 2)
      throw new IllegalArgumentException(
          "Shingles of sizes less than 2 are just plain terms, foo!");
    this.maxShingleSize = maxShingleSize;
  }

  @Override
  public TokenFilter getTokenStream(TokenStream tokenStream) {
    return new PrefixShingleFilter(tokenStream, maxShingleSize);
  }

}
