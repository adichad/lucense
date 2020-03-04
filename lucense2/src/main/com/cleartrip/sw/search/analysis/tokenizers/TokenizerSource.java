package com.cleartrip.sw.search.analysis.tokenizers;

import java.io.Reader;
import java.util.Map;
import java.util.Properties;

import org.apache.lucene.analysis.TokenStream;

public abstract class TokenizerSource {
  public TokenizerSource(Map<String, ?> params, Properties env) {
    
  }
  public abstract TokenStream getTokenStream(Reader reader) throws Exception;
}
