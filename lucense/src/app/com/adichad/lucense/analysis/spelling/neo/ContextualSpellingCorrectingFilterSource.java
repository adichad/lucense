package com.adichad.lucense.analysis.spelling.neo;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

import com.adichad.lucense.analysis.component.filter.TokenFilterSource;
import com.adichad.lucense.analysis.spelling.CorrectionParameters;

public class ContextualSpellingCorrectingFilterSource implements TokenFilterSource {

  FixedSizeContextCorpus graph;

  private CorrectionParameters params;

  public ContextualSpellingCorrectingFilterSource(String graphPath, CorrectionParameters params) throws IOException {
    this.graph = new FixedSizeContextCorpus(graphPath);
    this.params = params;
  }

  @Override
  public TokenFilter getTokenFilter(TokenStream tokenStream) {
    return new ContextualSpellingCorrectingFilter(this.graph, this.params, tokenStream);
  }

}
