package com.cleartrip.sw.search.analysis.filters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

public class StopFilterSource extends TokenFilterSource {

  private final Set<String> stopwordSet;

  //private final boolean ignoreCase;

  private final Version matchVersion;

  public StopFilterSource(Map<String, ?> params, Properties env) throws Exception,
      FileNotFoundException {
    super(params, env);
    
    this.stopwordSet = new HashSet<String>();
    this.matchVersion = Version.valueOf((String)params.get("matchVersion"));
    String file=env.getProperty("path.etc")+File.separator+(String)params.get("stopwordFile");
    FileReader reader = new FileReader(file);
    TokenStream stopStream = new WhitespaceTokenizer(this.matchVersion, reader);
    CharTermAttribute termAttr = stopStream.addAttribute(CharTermAttribute.class);
    stopStream.reset();
    while (stopStream.incrementToken()) {
      this.stopwordSet.add(termAttr.toString());
    }
    stopStream.close();
    //this.ignoreCase = (Boolean)params.get("ignoreCase");
  }

  @Override
  public TokenFilter getTokenStream(TokenStream tokenStream) {
    return new StopFilter(matchVersion, tokenStream, this.stopwordSet);
  }

}
