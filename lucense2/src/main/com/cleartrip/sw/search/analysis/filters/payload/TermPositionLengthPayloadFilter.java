package com.cleartrip.sw.search.analysis.filters.payload;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.index.Payload;

public class TermPositionLengthPayloadFilter extends TokenFilter {

  private final PayloadAttribute           payAtt;
  private final PositionIncrementAttribute posIncrAttr;
  private final LinkedList<State>          states;
  private int                              size = 0;
  private final int                        offset;
  private final int                        len;
  private final int                        termOffset;

  public TermPositionLengthPayloadFilter(TokenStream input, int offset,
      int len, int termOffset) {
    super(input);
    this.payAtt = addAttribute(PayloadAttribute.class);
    this.posIncrAttr = addAttribute(PositionIncrementAttribute.class);
    this.states = new LinkedList<>();
    this.offset = offset;
    this.len = Math.max(len, offset + 1);
    this.termOffset = termOffset;
  }

  @Override
  public final void reset() throws IOException {
    size = 0;
    input.reset();
  }

  @Override
  public final boolean incrementToken() throws IOException {
    while (input.incrementToken()) {
      if (posIncrAttr.getPositionIncrement() > 0)
        size++;
      states.add(this.captureState());
    }
    if (states.isEmpty())
      return false;
    int csize = size - termOffset;
    this.restoreState(states.removeFirst());
    if (size > 0) {
      Payload payload = this.payAtt.getPayload();
      if (payload == null) {
        payload = new Payload(new byte[len]);
      }
      byte[] b = payload.getData();
      b[offset] = (byte) (csize > 127 ? 127 : csize);
      payload.setData(b);
      this.payAtt.setPayload(payload);
    }
    return true;
  }

}
