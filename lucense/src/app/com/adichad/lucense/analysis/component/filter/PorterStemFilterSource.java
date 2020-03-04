package com.adichad.lucense.analysis.component.filter;

import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

public class PorterStemFilterSource implements TokenFilterSource {

  public PorterStemFilterSource() {}

  @Override
  public TokenFilter getTokenFilter(TokenStream tokenStream) {
    return new PorterStemFilter(tokenStream);
  }

}
