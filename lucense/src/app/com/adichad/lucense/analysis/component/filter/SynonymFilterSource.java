package com.adichad.lucense.analysis.component.filter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

import com.adichad.lucense.analysis.synonym.AugmentingSynonymFilter;
import com.adichad.lucense.analysis.synonym.ReplacingSynonymFilter;
import com.adichad.lucense.analysis.synonym.SynonymMap;

public class SynonymFilterSource implements TokenFilterSource {

  private final SynonymMap<String, String> synonymMap;

  private boolean replace;

  public SynonymFilterSource(String file) throws IOException, FileNotFoundException {
    this.synonymMap = new SynonymMap<String, String>(null);
    List<String> m = new ArrayList<String>();
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
    this.replace = false;
    // System.out.println(synonymMap);
  }

  public SynonymFilterSource(String file, boolean replace) throws IOException, FileNotFoundException {
    this(file);
    this.replace = replace;
  }

  @Override
  public TokenFilter getTokenFilter(TokenStream tokenStream) {
    if (replace) {
      return new ReplacingSynonymFilter(tokenStream, this.synonymMap);
    } else {
      return new AugmentingSynonymFilter(tokenStream, this.synonymMap);
    }
  }

}
