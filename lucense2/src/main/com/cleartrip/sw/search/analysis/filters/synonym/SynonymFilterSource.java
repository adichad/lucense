package com.cleartrip.sw.search.analysis.filters.synonym;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

import com.cleartrip.sw.search.analysis.filters.TokenFilterSource;


public class SynonymFilterSource extends TokenFilterSource {

  private final SynonymMap<String, String> synonymMap;
  private Boolean replace;

  public SynonymFilterSource(Map<String, ?> params, Properties env) throws Exception {
    super(params, env);
    this.synonymMap = new SynonymMap<String, String>(null);
    List<String> m = new ArrayList<String>();
    String file=env.getProperty("path.etc")+File.separator+(String)params.get("synonymFile");
    BufferedReader r = new BufferedReader(new FileReader(file));
    String line;
    while ((line = r.readLine()) != null) {
      String[] keyval = line.split(":", 2);
      String[] vals = keyval[1].split(",", 0);
      for (String val : vals) {
        String[] words = val.split("\\s+");
        for (String word : words)
          m.add(word);
        this.synonymMap.put(m, keyval[0]);
        m.clear();
      }
    }
    r.close();
    this.replace = (Boolean)params.get("replace");
  }

  @Override
  public TokenFilter getTokenStream(TokenStream tokenStream) {
    if (replace) {
      return new ReplacingSynonymFilter(tokenStream, this.synonymMap);
    } else {
      return new AugmentingSynonymFilter(tokenStream, this.synonymMap);
    }
  }

}
