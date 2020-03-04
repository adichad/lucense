package com.cleartrip.sw.search.analysis.filters;

import java.util.Map;
import java.util.Properties;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Version;

public class LowerCaseFilterSource extends TokenFilterSource {

  private final Version matchVersion;

  public LowerCaseFilterSource(Map<String, ?> params, Properties env) throws Exception {
    super(params, env);
    this.matchVersion = Version.valueOf((String)params.get("matchVersion"));
  }

  @Override
  public TokenFilter getTokenStream(TokenStream tokenStream) {
    return new LowerCaseFilter(matchVersion, tokenStream);
  }

}
