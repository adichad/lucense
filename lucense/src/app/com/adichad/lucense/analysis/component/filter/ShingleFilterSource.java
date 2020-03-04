package com.adichad.lucense.analysis.component.filter;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.shingle.ShingleFilter;

public class ShingleFilterSource implements TokenFilterSource {

  private final int maxShingleSize;

  public ShingleFilterSource(int maxShingleSize) {
    if (maxShingleSize < 2)
      throw new IllegalArgumentException(
          "Shingles of sizes less than 2 are just plain terms, foo!");
    this.maxShingleSize = maxShingleSize;
  }

  @Override
  public TokenFilter getTokenFilter(TokenStream tokenStream) {
    return new ShingleFilter(tokenStream, maxShingleSize);
  }

}
