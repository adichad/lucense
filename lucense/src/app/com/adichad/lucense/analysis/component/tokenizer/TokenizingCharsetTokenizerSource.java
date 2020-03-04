package com.adichad.lucense.analysis.component.tokenizer;

import java.io.Reader;

import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Version;

public class TokenizingCharsetTokenizerSource implements TokenStreamSource {

  private final String tokenCharString;

  private final Version matchVersion;

  public TokenizingCharsetTokenizerSource(Version matchVersion, String tokenChars) {
    this.tokenCharString = tokenChars;
    this.matchVersion = matchVersion;
  }

  @Override
  public TokenStream getTokenStream(Reader reader) {
    return new CharTokenizer(matchVersion, reader) {
      private final String tokenChars = TokenizingCharsetTokenizerSource.this.tokenCharString;

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
