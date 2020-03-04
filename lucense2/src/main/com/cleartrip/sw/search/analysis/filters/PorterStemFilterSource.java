package com.cleartrip.sw.search.analysis.filters;

import java.util.Map;
import java.util.Properties;

import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

public class PorterStemFilterSource extends TokenFilterSource {

  public PorterStemFilterSource(Map<String, ?> params, Properties env) throws Exception {
    super(params, env);
  }

  @Override
  public TokenFilter getTokenStream(TokenStream tokenStream) {
    return new PorterStemFilter(tokenStream);
  }

}
