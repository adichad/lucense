package com.adichad.lucense.analysis.component.tokenizer;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.TokenStream;

import com.adichad.lucense.analysis.tokenizer.PatternTokenizer;

public class PatternTokenizerSource implements TokenStreamSource {
  private Pattern pattern;

  public PatternTokenizerSource(String patString) {
    this.pattern = Pattern.compile(patString);
  }

  @Override
  public TokenStream getTokenStream(Reader reader) throws IOException {
    return new PatternTokenizer(reader, this.pattern);
  }

}
