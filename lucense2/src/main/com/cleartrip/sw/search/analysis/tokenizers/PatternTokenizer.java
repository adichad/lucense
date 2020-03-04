package com.cleartrip.sw.search.analysis.tokenizers;

import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

public class PatternTokenizer extends TokenStream {

  private final String str;

  private Matcher matcher;

  private CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

  private OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

  public PatternTokenizer(Reader reader, Pattern pattern) throws IOException {
    this.str = toString(reader);
    this.matcher = pattern.matcher(this.str);
  }

  @Override
  public final boolean incrementToken() {
    clearAttributes();
    int start, end;
    if (this.matcher.find()) {
      start = this.matcher.start();
      end = this.matcher.end();
      this.termAtt.setEmpty().append(this.str.substring(start, end));
      this.offsetAtt.setOffset(start, end);
      return true;
    }
    return false;
  }

  private static String toString(Reader input) throws IOException {
    try {
      int len = 256;
      char[] buffer = new char[len];
      char[] output = new char[len];

      len = 0;
      int n;
      while ((n = input.read(buffer)) >= 0) {
        if (len + n > output.length) { // grow capacity
          char[] tmp = new char[Math.max(output.length << 1, len + n)];
          System.arraycopy(output, 0, tmp, 0, len);
          System.arraycopy(buffer, 0, tmp, len, n);
          buffer = output; // use larger buffer for future larger bulk reads
          output = tmp;
        } else {
          System.arraycopy(buffer, 0, output, len, n);
        }
        len += n;
      }

      return new String(output, 0, len);
    } finally {
      if (input != null)
        input.close();
    }
  }
}
