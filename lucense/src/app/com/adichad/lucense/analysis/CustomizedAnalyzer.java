package com.adichad.lucense.analysis;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;

import com.adichad.lucense.analysis.component.filter.TokenFilterSource;
import com.adichad.lucense.analysis.component.tokenizer.TokenStreamSource;

public class CustomizedAnalyzer extends Analyzer {

  private TokenStreamSource tokenStreamSource;

  private List<TokenFilterSource> filterSources;

  public CustomizedAnalyzer(TokenStreamSource tokenStreamSource, List<TokenFilterSource> filterSources) {
    this.tokenStreamSource = tokenStreamSource;
    if (filterSources == null) {
      filterSources = new ArrayList<TokenFilterSource>();
    }
    this.filterSources = filterSources;
  }

  @Override
  public TokenStream tokenStream(String fieldName, Reader reader) {
    try {
      TokenStream tokStream = this.tokenStreamSource.getTokenStream(reader);
      for (TokenFilterSource filterSource : this.filterSources)
        tokStream = filterSource.getTokenFilter(tokStream);
      return tokStream;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
