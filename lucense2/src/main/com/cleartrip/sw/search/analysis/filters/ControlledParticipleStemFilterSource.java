package com.cleartrip.sw.search.analysis.filters;

import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import com.cleartrip.sw.search.analysis.filters.stem.ControlledParticipleStemFilter;
import com.cleartrip.sw.search.util.Constants;

public class ControlledParticipleStemFilterSource extends TokenFilterSource {

  private Set<String> blackList;

  private int         minLen;

  private boolean     replace;

  private boolean     markStem;

  public ControlledParticipleStemFilterSource(Map<String, ?> params,
      Properties env) throws Exception {
    super(params, env);
    this.blackList = new HashSet<String>();
    Version matchVersion = Version.valueOf((String) params.get("matchVersion"));
    String file = (String) params.get("excludeFile");
    if (file != null) {
      FileReader reader = new FileReader(
          env.getProperty(Constants.ETC_PATH_KEY) + File.separator + file);
      TokenStream blackListStream = new WhitespaceTokenizer(matchVersion,
          reader);
      CharTermAttribute termAttr = blackListStream
          .addAttribute(CharTermAttribute.class);
      blackListStream.reset();
      while (blackListStream.incrementToken()) {
        this.blackList.add(termAttr.toString());
      }
      blackListStream.close();
    }
    this.minLen = (Integer) params.get("minLen");
    this.replace = (Boolean) params.get("replace");
    this.markStem = false;
  }

  @Override
  public TokenStream getTokenStream(TokenStream tokenStream) {
    return new ControlledParticipleStemFilter(tokenStream, this.blackList,
        this.minLen, this.replace, this.markStem);
  }

}
