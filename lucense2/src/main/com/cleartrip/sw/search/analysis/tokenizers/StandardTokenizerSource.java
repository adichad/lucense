package com.cleartrip.sw.search.analysis.tokenizers;

import java.io.Reader;
import java.util.Map;
import java.util.Properties;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;


public class StandardTokenizerSource extends TokenizerSource {

  private final Version version;

  public StandardTokenizerSource(Map<String, ?> params, Properties env) {
    super(params, env);
    this.version = Version.valueOf((String)params.get("matchVersion"));
  }

  @Override
  public TokenStream getTokenStream(Reader reader) {
    return new StandardTokenizer(this.version, reader);
  }

}
