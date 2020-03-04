package com.cleartrip.sw.search.analysis.filters;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class PrefixShingleFilter extends TokenFilter {

  private final int               maxShingleSize;
  private int                     curShingleSize = 0;
  private StringBuilder           shingleBuilder = new StringBuilder();
  private final CharTermAttribute tAttr;

  protected PrefixShingleFilter(TokenStream input, int maxShingleSize) {
    super(input);
    this.maxShingleSize = maxShingleSize;
    this.tAttr = addAttribute(CharTermAttribute.class);
  }

  @Override
  public void reset() throws IOException {
    input.reset();
    shingleBuilder = new StringBuilder();
    curShingleSize = 0;
  }

  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken() && curShingleSize++ < maxShingleSize) {
      shingleBuilder.append(tAttr.buffer(), 0, tAttr.length());
      tAttr.setEmpty().append(shingleBuilder);
      shingleBuilder.append(" ");
      return true;
    }
    return false;
  }

}
