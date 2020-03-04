package com.cleartrip.sw.search.analysis.tokenizers;

import java.io.Reader;
import java.util.Map;
import java.util.Properties;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.util.Version;


public class WhitespaceTokenizerSource extends TokenizerSource {
  private final Version matchVersion;

  public WhitespaceTokenizerSource(Map<String, ?> params, Properties env) {
    super(params, env);
    this.matchVersion = Version.valueOf((String)params.get("matchVersion"));
  }

  @Override
  public TokenStream getTokenStream(Reader reader) {
    return new WhitespaceTokenizer(matchVersion, reader);
  }

}
