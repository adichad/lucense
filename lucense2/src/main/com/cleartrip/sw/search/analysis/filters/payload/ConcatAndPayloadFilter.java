package com.cleartrip.sw.search.analysis.filters.payload;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
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
import com.cleartrip.sw.search.analysis.filters.ConcatAndPayloadFilterSource;
import com.cleartrip.sw.search.analysis.filters.TokenFilterSource;
import com.cleartrip.sw.search.analysis.tokenizers.StandardTokenizerSource;

public class ConcatAndPayloadFilter extends TokenFilter {

  private final PayloadAttribute payAtt;
  private StringBuilder          sb;
  private CharTermAttribute      termAttr;
  private final int              offset;
  private final int              len;

  public ConcatAndPayloadFilter(TokenStream input, int offset, int len) {
    super(input);
    this.payAtt = addAttribute(PayloadAttribute.class);
    this.termAttr = addAttribute(CharTermAttribute.class);
    this.sb = new StringBuilder();
    this.offset = offset;
    this.len = Math.max(len, offset+4);
  }

  @Override
  public final void reset() throws IOException {
    input.reset();
    sb = new StringBuilder();
  }

  @Override
  public final boolean incrementToken() throws IOException {
    int lastSize = 0;
    boolean hasmore = false;
    while (input.incrementToken()) {
      hasmore = true;
      this.sb.append(termAttr.buffer(), 0, termAttr.length()).append(" ");
      lastSize = termAttr.length() + 1;
    }

    if (!hasmore)
      return false;
    char[] lastVal = new char[lastSize - 1];
    sb.getChars(sb.length() - lastSize, sb.length() - 1, lastVal, 0);
    int payloadInt = Integer.parseInt(new String(lastVal));
    sb.delete(sb.length() - lastSize - 1, sb.length());
    this.termAttr.copyBuffer(sb.toString().toCharArray(), 0, sb.length());

    if (payloadInt > 0) {
      byte[] payloadBytes = ByteBuffer.allocate(4).putInt(payloadInt).array();
      Payload payload = this.payAtt.getPayload();
      if (payload == null) {
        payload = new Payload(new byte[len]);
      }
      byte[] bs = payload.getData();
      for (short i = 0; i < 4; ++i) {
        bs[i + offset] = payloadBytes[i];
      }
      this.payAtt.setPayload(payload);
      // System.out.println(sb.toString()+": "+payloadInt);
    }

    return true;
  }

  public static void main(String[] args) throws Exception {
    StringReader[] input = { new StringReader("first:100000"),
        new StringReader("my second:0"), new StringReader("my second:100000"),
        new StringReader("my second:0") };
    Map<String, String> props = new HashMap<>();
    props.put("matchVersion", Version.LUCENE_36.toString());
    List<CharFilterSource> charFilters = new LinkedList<CharFilterSource>();
    List<TokenFilterSource> tokenFilters = new LinkedList<TokenFilterSource>();
    tokenFilters.add(new ConcatAndPayloadFilterSource(props, null));

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
          System.out.print("=>["
              + ByteBuffer.wrap(pAttr.getPayload().getData(), 1, 4).getInt()
              + "]");
        System.out.println();
      }
      f.close();
    }
    a.close();
  }

}
