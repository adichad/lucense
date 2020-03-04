package com.cleartrip.sw.search.analysis.tokenizers;

import java.io.Reader;
import java.util.Map;
import java.util.Properties;

import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Version;


public class TokenizingCharTokenizerSource extends TokenizerSource {

  private final String tokenCharString;

  private final Version matchVersion;

  public TokenizingCharTokenizerSource(Map<String, ?> params, Properties env) {
    super(params, env);
    this.tokenCharString = (String)params.get("tokenizingChars");
    this.matchVersion = Version.valueOf((String)params.get("matchVersion"));
  }

  @Override
  public TokenStream getTokenStream(Reader reader) {
    return new CharTokenizer(matchVersion, reader) {
      private final String tokenChars = TokenizingCharTokenizerSource.this.tokenCharString;

      @Override
      protected boolean isTokenChar(int c) {
        return this.tokenChars.indexOf(c) >= 0 ? false : true;
      }

      @Override
      protected int normalize(int c) {
        return c;
      }
    };
  }

}
