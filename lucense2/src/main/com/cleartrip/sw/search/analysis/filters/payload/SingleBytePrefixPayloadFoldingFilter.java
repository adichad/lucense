package com.cleartrip.sw.search.analysis.filters.payload;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.index.Payload;
import org.apache.lucene.util.Version;

import com.cleartrip.sw.search.analysis.GenericAnalyzer;
import com.cleartrip.sw.search.analysis.charfilters.CharFilterSource;
import com.cleartrip.sw.search.analysis.filters.SingleBytePrefixPayloadFoldingFilterSource;
import com.cleartrip.sw.search.analysis.filters.TokenFilterSource;
import com.cleartrip.sw.search.analysis.tokenizers.StandardTokenizerSource;

public class SingleBytePrefixPayloadFoldingFilter extends TokenFilter {

  private final PayloadAttribute payAtt;
  private Byte                   payload;
  private CharTermAttribute      termAttr;
  private final int              offset;
  private final int              len;

  public SingleBytePrefixPayloadFoldingFilter(TokenStream input, int offset,
      int len) {
    super(input);
    this.payAtt = addAttribute(PayloadAttribute.class);
    this.termAttr = addAttribute(CharTermAttribute.class);
    this.offset = offset;
    this.len = Math.max(len, offset + 1);
    this.payload = null;
  }

  @Override
  public final void reset() throws IOException {
    input.reset();
    payload = null;
  }

  @Override
  public final boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      if (this.payload == null) {
        this.payload = Byte.parseByte(termAttr.toString());
        if (!input.incrementToken())
          return false;
      }
      if (this.payload != 0) {
        Payload payload = this.payAtt.getPayload();
        if (payload == null) {
          payload = new Payload(new byte[len]);
        }
        byte[] bs = payload.getData();
        bs[offset] = this.payload;
        payload.setData(bs);
        this.payAtt.setPayload(payload);
      }
      return true;
    }
    return false;
  }

  public static void main(String[] args) throws Exception {
    StringReader[] input = { new StringReader("1 first"),
        new StringReader("0 myll second"),
        new StringReader("1 myll second"),
        new StringReader("0 myll second") };
    Map<String, Object> props = new HashMap<>();
    int offset = 1;
    props.put("matchVersion", Version.LUCENE_36.toString());
    props.put("payloadOffset", offset);
    props.put("payloadLen", 2);
    List<CharFilterSource> charFilters = new LinkedList<CharFilterSource>();
    List<TokenFilterSource> tokenFilters = new LinkedList<TokenFilterSource>();
    tokenFilters.add(new SingleBytePrefixPayloadFoldingFilterSource(props, null));

    GenericAnalyzer a = new GenericAnalyzer(charFilters,
        new StandardTokenizerSource(props, null), tokenFilters, 100, 100);
    for (int i = 0; i < input.length; i++) {
      TokenStream f = a.reusableTokenStream("", input[i]);
      CharTermAttribute tAttr = f.addAttribute(CharTermAttribute.class);
      PayloadAttribute pAttr = f.addAttribute(PayloadAttribute.class);
      while (f.incrementToken()) {
        System.out.print("[" + tAttr.toString() + "]");
        Payload p = pAttr.getPayload();
        if (p != null)
          System.out.print("=>[" + pAttr.getPayload().getData()[offset] + "]");
        System.out.println();
      }
      f.close();
    }
    a.close();
  }

}
