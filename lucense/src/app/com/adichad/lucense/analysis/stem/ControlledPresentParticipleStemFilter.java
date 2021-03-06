package com.adichad.lucense.analysis.stem;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Set;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Payload;

public class ControlledPresentParticipleStemFilter extends TokenFilter {
  private ControlledPresentParticipleStemmer stemmer;

  private static final int MASK_BYTE_ID = 0;

  private static final int MASK_BIT_ID = 1;

  private CharTermAttribute termAtt;

  private boolean replaceFlag;

  CharBuffer buff;

  private PositionIncrementAttribute posAtt;

  private boolean writeStem;

  private PayloadAttribute payAtt;

  private boolean markStem = false;

  // private static final CharBuffer pre = CharBuffer.wrap("[cstem0]", 0, 6);

  public ControlledPresentParticipleStemFilter(TokenStream input) {
    super(input);
    this.stemmer = new ControlledPresentParticipleStemmer();
    this.termAtt = addAttribute(CharTermAttribute.class);
    this.posAtt = addAttribute(PositionIncrementAttribute.class);
    this.payAtt = addAttribute(PayloadAttribute.class);
    this.replaceFlag = true;
    this.writeStem = false;
  }

  public ControlledPresentParticipleStemFilter(TokenStream input, Set<String> blackList, boolean markStem) {
    super(input);
    this.stemmer = new ControlledPresentParticipleStemmer(blackList);
    this.termAtt = addAttribute(CharTermAttribute.class);
    this.posAtt = addAttribute(PositionIncrementAttribute.class);
    this.payAtt = addAttribute(PayloadAttribute.class);
    this.replaceFlag = true;
    this.writeStem = false;

    this.markStem = markStem;
  }

  public ControlledPresentParticipleStemFilter(TokenStream input, Set<String> blackList, int minLen, boolean replace,
      boolean markStem) {
    super(input);
    this.stemmer = new ControlledPresentParticipleStemmer(blackList, minLen);
    this.termAtt = addAttribute(CharTermAttribute.class);
    this.posAtt = addAttribute(PositionIncrementAttribute.class);
    this.payAtt = addAttribute(PayloadAttribute.class);
    this.replaceFlag = replace;
    this.writeStem = false;
    this.markStem = markStem;
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
        if (this.markStem)
          setPayload();
      }
      return true;
    }
    if (this.writeStem) {
      this.posAtt.setPositionIncrement(0);
      this.termAtt.copyBuffer(this.buff.array(), 0, this.buff.length());// stemmer.getResultLength()
      if (this.markStem)
        setPayload();
      this.writeStem = false;
      return true;
    }
    if (!this.input.incrementToken())
      return false;
    if (this.stemmer.stem(this.termAtt.buffer(), 0, this.termAtt.length())) {
      this.buff = (CharBuffer.wrap(this.stemmer.getResultBuffer(), 0, this.stemmer.getResultLength()));
      this.writeStem = true;
    }
    return true;
  }

  private void setPayload() {
    Payload payload = this.payAtt.getPayload();
    if (payload == null) {
      payload = new Payload(new byte[1]);
    }
    byte[] b = payload.getData();
    b[MASK_BYTE_ID] |= (1 << MASK_BIT_ID);
    payload.setData(b);
    this.payAtt.setPayload(payload);
  }
}
