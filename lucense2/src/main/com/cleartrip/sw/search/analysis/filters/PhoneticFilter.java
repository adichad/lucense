package com.cleartrip.sw.search.analysis.filters;

import java.io.IOException;

import org.apache.commons.codec.Encoder;
import org.apache.commons.codec.EncoderException;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

public class PhoneticFilter extends TokenFilter {
  protected boolean inject = true;

  protected Encoder encoder = null;

  protected Token save = null;

  private CharTermAttribute termAtt;

  private PositionIncrementAttribute posAtt;

  private boolean writeEncoding;

  private String encoding;

  //private static final String pre = "[phon]";

  public PhoneticFilter(TokenStream input, Encoder encoder, boolean inject) throws Exception {
    super(input);
    this.encoder = encoder;
    this.inject = inject;
    this.termAtt = addAttribute(CharTermAttribute.class);
    this.posAtt = addAttribute(PositionIncrementAttribute.class);
    this.writeEncoding = false;
  }

  @Override
  public final boolean incrementToken() throws IOException {
    if (!this.inject) {
      if (!this.input.incrementToken())
        return false;
      try {
        this.termAtt.setEmpty().append(this.encoder.encode(this.termAtt.toString()).toString());
      } catch (EncoderException e) {
      }
      return true;
    }
    if (this.writeEncoding) {
      this.posAtt.setPositionIncrement(0);
      this.termAtt.setEmpty().append(this.encoding);
      this.writeEncoding = false;
      return true;
    }
    if (!this.input.incrementToken())
      return false;
    try {
      String val = this.encoder.encode(this.termAtt.toString()).toString();
      if (!val.equals(this.termAtt.toString())) {
        this.encoding = val;//pre.concat(val);
        this.writeEncoding = true;
      } else
        this.writeEncoding = false;
    } catch (EncoderException e) {
      this.writeEncoding = false;
    }
    return true;
  }
}
