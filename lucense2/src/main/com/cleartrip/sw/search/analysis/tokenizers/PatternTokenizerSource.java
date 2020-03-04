package com.cleartrip.sw.search.analysis.tokenizers;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.TokenStream;



public class PatternTokenizerSource extends TokenizerSource {
  private Pattern pattern;

  public PatternTokenizerSource(Map<String, ?> params, Properties env) {
    super(params, env);
    this.pattern = Pattern.compile((String)params.get("pattern"));
  }

  @Override
  public TokenStream getTokenStream(Reader reader) throws IOException {
    return new PatternTokenizer(reader, this.pattern);
  }

}
