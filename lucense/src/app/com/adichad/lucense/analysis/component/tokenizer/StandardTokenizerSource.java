package com.adichad.lucense.analysis.component.tokenizer;

import java.io.Reader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

public class StandardTokenizerSource implements TokenStreamSource {

  private final Version version;

  public StandardTokenizerSource(Version v) {
    this.version = v;
  }

  @Override
  public TokenStream getTokenStream(Reader reader) {
    return new StandardTokenizer(this.version, reader);
  }

}
