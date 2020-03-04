package com.cleartrip.sw.search.analysis.filters;

import java.util.Map;
import java.util.Properties;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.util.Version;

public class StandardFilterSource extends TokenFilterSource {

  private final Version matchVersion;

  public StandardFilterSource(Map<String, ?> params, Properties env) throws Exception {
    super(params, env);
    this.matchVersion = Version.valueOf((String)params.get("matchVersion"));
  }

  @Override
  public TokenStream getTokenStream(TokenStream ts) throws Exception {
    return new StandardFilter(this.matchVersion, ts);
  }

}
