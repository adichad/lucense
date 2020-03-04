package com.adichad.lucense.analysis.component.tokenizer;

import java.io.Reader;

import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Version;

public class CharTokenizerSource implements TokenStreamSource {

  private final String tokenCharString;

  private final Version matchVersion;

  public enum CharNormalizerType {
    NONE;
  }

  public CharTokenizerSource(Version matchVersion, String tokenChars, CharNormalizerType normType) {
    this.tokenCharString = tokenChars;
    this.matchVersion = matchVersion;
  }

  @Override
  public TokenStream getTokenStream(Reader reader) {
    return new CharTokenizer(matchVersion, reader) {
      private final String tokenChars = CharTokenizerSource.this.tokenCharString;

      @Override
      protected boolean isTokenChar(int c) {
        return this.tokenChars.indexOf(c) >= 0;
      }

      @Override
      protected int normalize(int c) {
        return c;
      }

    };
  }

}
