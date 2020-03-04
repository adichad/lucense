package com.adichad.lucense.analysis.stem;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Set;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class ControlledPluralsInvertingStemFilter extends TokenFilter {
  private ControlledPluralsInvertingStemmer stemmer;

  private CharTermAttribute termAtt;

  private boolean replaceFlag;

  CharBuffer buff;

  private PositionIncrementAttribute posAtt;

  private boolean writeStem;

  private StemInversionAttribute invAtt;

  // private static final CharBuffer pre = CharBuffer.wrap("[cstem0]", 0, 6);

  public ControlledPluralsInvertingStemFilter(TokenStream input) {
    super(input);
    this.invAtt = addAttribute(StemInversionAttribute.class);
    this.stemmer = new ControlledPluralsInvertingStemmer(this.invAtt);
    this.termAtt = addAttribute(CharTermAttribute.class);
    this.posAtt = addAttribute(PositionIncrementAttribute.class);
    this.replaceFlag = true;
    this.writeStem = false;
  }

  public ControlledPluralsInvertingStemFilter(TokenStream input, Set<String> blackList) {
    super(input);
    this.invAtt = addAttribute(StemInversionAttribute.class);
    this.stemmer = new ControlledPluralsInvertingStemmer(blackList, this.invAtt);
    this.termAtt = addAttribute(CharTermAttribute.class);
    this.posAtt = addAttribute(PositionIncrementAttribute.class);
    this.replaceFlag = true;
    this.writeStem = false;
  }

  public ControlledPluralsInvertingStemFilter(TokenStream input, Set<String> blackList, int minLen, boolean replace) {
    super(input);
    this.invAtt = addAttribute(StemInversionAttribute.class);
    this.stemmer = new ControlledPluralsInvertingStemmer(blackList, minLen, this.invAtt);
    this.termAtt = addAttribute(CharTermAttribute.class);
    this.posAtt = addAttribute(PositionIncrementAttribute.class);

    this.replaceFlag = replace;
    this.writeStem = false;
  }

  @Override
  public final boolean incrementToken() throws IOException {
    if (this.replaceFlag) {
      if (!this.input.incrementToken())
        return false;
      if (this.stemmer.stem(this.termAtt.buffer(), 0, this.termAtt.length())) {
        // buff = pre.append(CharBuffer.wrap(stemmer.getResultBuffer(), 0,
        // stemmer.getResultLength()));

        this.termAtt.copyBuffer(this.stemmer.getResultBuffer(), 0, this.stemmer.getResultLength());
      }
      return true;
    }
    if (this.writeStem) {
      this.posAtt.setPositionIncrement(0);
      this.termAtt.copyBuffer(this.buff.array(), 0, this.buff.length());// stemmer.getResultLength()
      this.writeStem = false;
      return true;
    }
    if (!this.input.incrementToken())
      return false;
    if (this.stemmer.stem(this.termAtt.buffer(), 0, this.termAtt.length())) {
      this.buff = CharBuffer.wrap(this.stemmer.getResultBuffer(), 0, this.stemmer.getResultLength());
      this.writeStem = true;
    }
    return true;
  }

}
