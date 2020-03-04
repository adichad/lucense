package com.cleartrip.sw.search.analysis;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharReader;
import org.apache.lucene.analysis.CharStream;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Fieldable;

import com.cleartrip.sw.search.analysis.charfilters.CharFilterSource;
import com.cleartrip.sw.search.analysis.filters.TokenFilterSource;
import com.cleartrip.sw.search.analysis.tokenizers.TokenizerSource;

public class GenericAnalyzer extends Analyzer {

  private final TokenizerSource tokenizerSource;
  private final List<TokenFilterSource> filterSources;
  private final List<CharFilterSource> charFilterSources;
  
  private final int offsetGap;
  private final int positionIncrementGap;
  
  public GenericAnalyzer(List<CharFilterSource> charFilterSources, TokenizerSource tokenizerSource,
      List<TokenFilterSource> filterSources, int positionIncrementGap, int offsetGap) {
    this.tokenizerSource = tokenizerSource;
    if (filterSources == null) {
      filterSources = new ArrayList<TokenFilterSource>();
    }
    this.filterSources = filterSources;
    if (charFilterSources == null) {
      charFilterSources = new ArrayList<CharFilterSource>();
    }
    this.charFilterSources = charFilterSources;
    this.positionIncrementGap = positionIncrementGap;
    this.offsetGap = offsetGap;
  }

  @Override
  public TokenStream tokenStream(String fieldName, Reader reader) {
    try {
      if(!charFilterSources.isEmpty()) {
        reader = CharReader.get(reader);
        for(CharFilterSource charFilterSource: charFilterSources) {
          reader = charFilterSource.getCharFilter((CharStream)reader);
        }
      }
      TokenStream tokStream = tokenizerSource.getTokenStream(reader);
      for (TokenFilterSource filterSource : this.filterSources)
        tokStream = filterSource.getTokenStream(tokStream);
      return tokStream;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public int getPositionIncrementGap(String name) {
    return this.positionIncrementGap;
  }
  
  @Override
  public int getOffsetGap(Fieldable field) {
    if(field.isTokenized())
      return this.offsetGap;
    return 0;
  }

}
