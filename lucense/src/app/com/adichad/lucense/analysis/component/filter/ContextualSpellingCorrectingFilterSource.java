package com.adichad.lucense.analysis.component.filter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

import com.adichad.lucense.analysis.spelling.ContextualSpellingCorrectingFilter;
import com.adichad.lucense.analysis.spelling.CorrectionParameters;
import com.adichad.lucense.analysis.spelling.TermSequenceGraph;

public class ContextualSpellingCorrectingFilterSource implements TokenFilterSource {

  TermSequenceGraph graph;

  private CorrectionParameters params;

  public ContextualSpellingCorrectingFilterSource(String graphPath, CorrectionParameters params) throws IOException {
    InputStream in = new FileInputStream(graphPath);
    this.graph = TermSequenceGraph.readFrom(in);
    in.close();
    this.params = params;
  }

  @Override
  public TokenFilter getTokenFilter(TokenStream tokenStream) {
    return new ContextualSpellingCorrectingFilter(this.graph, this.params, tokenStream);
  }

}
