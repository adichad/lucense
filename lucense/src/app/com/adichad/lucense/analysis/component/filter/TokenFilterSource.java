package com.adichad.lucense.analysis.component.filter;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

public interface TokenFilterSource {
  public TokenFilter getTokenFilter(TokenStream tokenStream);

}
