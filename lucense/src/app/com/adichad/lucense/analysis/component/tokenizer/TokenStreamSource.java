package com.adichad.lucense.analysis.component.tokenizer;

import java.io.Reader;

import org.apache.lucene.analysis.TokenStream;

public interface TokenStreamSource {
  public TokenStream getTokenStream(Reader reader) throws Exception;
}
