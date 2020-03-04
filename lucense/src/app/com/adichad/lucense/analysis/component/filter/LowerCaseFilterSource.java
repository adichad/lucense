package com.adichad.lucense.analysis.component.filter;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Version;

public class LowerCaseFilterSource implements TokenFilterSource {

  private final Version matchVersion;

  public LowerCaseFilterSource(Version matchVersion) {
    this.matchVersion = matchVersion;
  }

  @Override
  public TokenFilter getTokenFilter(TokenStream tokenStream) {
    return new LowerCaseFilter(matchVersion, tokenStream);
  }

}
