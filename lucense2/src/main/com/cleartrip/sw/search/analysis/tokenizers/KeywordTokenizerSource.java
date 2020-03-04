package com.cleartrip.sw.search.analysis.tokenizers;

import java.io.Reader;
import java.util.Map;
import java.util.Properties;

import org.apache.lucene.analysis.KeywordTokenizer;
import org.apache.lucene.analysis.TokenStream;



public class KeywordTokenizerSource extends TokenizerSource {

  public KeywordTokenizerSource(Map<String, ?> params, Properties env) {
    super(params, env);
  }

  @Override
  public TokenStream getTokenStream(Reader reader) throws Exception {
    return new KeywordTokenizer(reader);
  }

}
