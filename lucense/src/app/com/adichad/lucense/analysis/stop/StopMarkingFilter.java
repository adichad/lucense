/*
 * @(#)com.adichad.lucense.analysis.stop.StopMarkingFilter.java
 * ===========================================================================
 * Licensed Materials - Property of InfoEdge 
 * "Restricted Materials of Adichad.Com" 
 * (C) Copyright <TBD> All rights reserved.
 * ===========================================================================
 */
package com.adichad.lucense.analysis.stop;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.index.Payload;
import org.apache.lucene.util.Version;

/**
 * 
 * Why oh why is StopFilter final, you crazy mofos?
 * 
 * @author adichad, Aditya Varun Chadha, Adichad.Com
 */
public class StopMarkingFilter extends TokenFilter {

  private final CharArraySet stopWords;

  private boolean enablePositionIncrements = false;

  private static final int MASK_BYTE_ID = 0;

  private static final int MASK_BIT_ID = 2;

  private CharTermAttribute termAtt;

  private PositionIncrementAttribute posIncrAtt;

  private PayloadAttribute payAtt;

  private IncrementToken incrImpl;

  public StopMarkingFilter(Version matchVersion, TokenStream input, Set<?> stopWords, boolean ignoreCase,
      boolean markStop) {
    super(input);
    if (stopWords instanceof CharArraySet) {
      this.stopWords = (CharArraySet) stopWords;
    } else {
      this.stopWords = new CharArraySet(matchVersion, stopWords.size(), ignoreCase);
      this.stopWords.addAll(stopWords);
    }
    this.enablePositionIncrements = matchVersion.onOrAfter(Version.LUCENE_29) ? true : false;
    termAtt = addAttribute(CharTermAttribute.class);
    posIncrAtt = addAttribute(PositionIncrementAttribute.class);

    if (markStop) {
      payAtt = addAttribute(PayloadAttribute.class);
      incrImpl = new MarkingIncrementToken();
    } else {
      incrImpl = new SkippingIncrementToken();
    }

  }

  private interface IncrementToken {
    public boolean incrementToken() throws IOException;
  }

  private class MarkingIncrementToken implements IncrementToken {
    @Override
    public boolean incrementToken() throws IOException {
      if (!input.incrementToken())
        return false;
      if (stopWords.contains(termAtt.buffer(), 0, termAtt.length())) {
        setPayload();
      }
      return true;
    }
  }

  private class SkippingIncrementToken implements IncrementToken {
    @Override
    public boolean incrementToken() throws IOException {
      int skippedPositions = 0;
      while (input.incrementToken()) {
        if (!stopWords.contains(termAtt.buffer(), 0, termAtt.length())) {
          if (enablePositionIncrements) {
            posIncrAtt.setPositionIncrement(posIncrAtt.getPositionIncrement() + skippedPositions);
          }
          return true;
        }
        skippedPositions += posIncrAtt.getPositionIncrement();
      }
      return false;
    }
  }

  public StopMarkingFilter(Version matchVersion, TokenStream in, Set<?> stopWords, boolean markStop) {
    this(matchVersion, in, stopWords, false, markStop);
  }

  public static final Set<Object> makeStopSet(String... stopWords) {
    return makeStopSet(stopWords, false);
  }

  public static final Set<Object> makeStopSet(List<?> stopWords) {
    return makeStopSet(stopWords, false);
  }

  public static final Set<Object> makeStopSet(String[] stopWords, boolean ignoreCase) {
    CharArraySet stopSet = new CharArraySet(Version.valueOf("LUCENE_29"), stopWords.length, ignoreCase);
    stopSet.addAll(Arrays.asList(stopWords));
    return stopSet;
  }

  public static final Set<Object> makeStopSet(List<?> stopWords, boolean ignoreCase) {
    CharArraySet stopSet = new CharArraySet(Version.valueOf("LUCENE_29"), stopWords.size(), ignoreCase);
    stopSet.addAll(stopWords);
    return stopSet;
  }

  @Override
  public final boolean incrementToken() throws IOException {
    return incrImpl.incrementToken();
  }

  public static boolean getEnablePositionIncrementsVersionDefault(Version matchVersion) {
    return matchVersion.onOrAfter(Version.LUCENE_29);
  }

  public boolean getEnablePositionIncrements() {
    return enablePositionIncrements;
  }

  public void setEnablePositionIncrements(boolean enable) {
    this.enablePositionIncrements = enable;
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
