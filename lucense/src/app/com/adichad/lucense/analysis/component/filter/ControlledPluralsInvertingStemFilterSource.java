package com.adichad.lucense.analysis.component.filter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import com.adichad.lucense.analysis.stem.ControlledPluralsInvertingStemFilter;

public class ControlledPluralsInvertingStemFilterSource implements TokenFilterSource {

  private HashSet<String> blackList;

  private int minLen;

  private boolean replace;

  private static Logger errorLogger = Logger.getLogger("ErrorLogger");

  public ControlledPluralsInvertingStemFilterSource(Version matchVersion, String file, int minLen, boolean replace) {
    this.blackList = new HashSet<String>();
    try {
      FileReader reader = new FileReader(file);
      TokenStream blackListStream = new WhitespaceTokenizer(matchVersion, reader);
      CharTermAttribute termAttr = blackListStream.addAttribute(CharTermAttribute.class);
      blackListStream.reset();
      while (blackListStream.incrementToken()) {
        this.blackList.add(termAttr.toString());
      }
    } catch (FileNotFoundException e) {
      errorLogger.log(Level.WARN, "Blacklist file not found: " + e);
    } catch (IOException e) {
      errorLogger.log(Level.WARN, e.getMessage());
    }
    this.minLen = minLen;
    this.replace = replace;
  }

  @Override
  public TokenFilter getTokenFilter(TokenStream tokenStream) {
    return new ControlledPluralsInvertingStemFilter(tokenStream, this.blackList, this.minLen, this.replace);
  }

}
