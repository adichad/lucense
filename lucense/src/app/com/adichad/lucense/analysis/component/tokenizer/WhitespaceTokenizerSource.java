package com.adichad.lucense.analysis.component.tokenizer;

import java.io.Reader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.util.Version;

public class WhitespaceTokenizerSource implements TokenStreamSource {
  private final Version matchVersion;

  public WhitespaceTokenizerSource(Version matchVersion) {
    this.matchVersion = matchVersion;
  }

  @Override
  public TokenStream getTokenStream(Reader reader) {
    return new WhitespaceTokenizer(matchVersion, reader);
  }

}
