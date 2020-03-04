package com.adichad.lucense.analysis.component.filter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import com.adichad.lucense.analysis.stop.StopMarkingFilter;

public class StopFilterSource implements TokenFilterSource {

  private final Set<String> stopwordSet;

  private final boolean markStop;

  private final Version matchVersion;

  public StopFilterSource(Version matchVersion, String file, boolean markStop) throws IOException,
      FileNotFoundException {
    this.stopwordSet = new HashSet<String>();
    this.matchVersion = matchVersion;
    FileReader reader = new FileReader(file);
    TokenStream stopStream = new WhitespaceTokenizer(this.matchVersion, reader);
    CharTermAttribute termAttr = stopStream.addAttribute(CharTermAttribute.class);
    stopStream.reset();
    while (stopStream.incrementToken()) {
      this.stopwordSet.add(termAttr.toString());
    }
    this.markStop = markStop;
  }

  @Override
  public TokenFilter getTokenFilter(TokenStream tokenStream) {
    if (markStop)
      return new StopMarkingFilter(matchVersion, tokenStream, this.stopwordSet, true);
    return new StopFilter(matchVersion, tokenStream, this.stopwordSet, false);
  }

}
