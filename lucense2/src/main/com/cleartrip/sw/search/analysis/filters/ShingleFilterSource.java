package com.cleartrip.sw.search.analysis.filters;

import java.util.Map;
import java.util.Properties;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.shingle.ShingleFilter;

public class ShingleFilterSource extends TokenFilterSource {

  private final int maxShingleSize;

  public ShingleFilterSource(Map<String, ?> params, Properties env) throws Exception {
    super(params, env);
    int maxShingleSize = (Integer)params.get("maxShingleSize");
    if (maxShingleSize < 2)
      throw new IllegalArgumentException(
          "Shingles of sizes less than 2 are just plain terms, foo!");
    this.maxShingleSize = maxShingleSize;
  }

  @Override
  public TokenFilter getTokenStream(TokenStream tokenStream) {
    return new ShingleFilter(tokenStream, maxShingleSize);
  }

}
