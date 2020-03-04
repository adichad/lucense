package com.adichad.lucense.analysis.component.filter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

import com.adichad.lucense.analysis.synonym.HighlightObject;
import com.adichad.lucense.analysis.synonym.InvertingReplacingSynonymFilter;
import com.adichad.lucense.analysis.synonym.SynonymMap;

public class InvertingReplacingSynonymFilterSource implements TokenFilterSource {

  private final SynonymMap<String, String> synonymMap;

  private final HashMap<String, HighlightObject> highlightObj;

  public InvertingReplacingSynonymFilterSource(String file) throws IOException, FileNotFoundException {

    this.highlightObj = new HashMap<String, HighlightObject>();

    this.synonymMap = new SynonymMap<String, String>(null);

    List<String> m = new ArrayList<String>();
    BufferedReader r = new BufferedReader(new FileReader(file));
    String line;

    HighlightObject obj;
    int maxSpan = 0;
    HashSet<String> values;

    while ((line = r.readLine()) != null) {

      values = new HashSet<String>();
      maxSpan = 0;

      String[] keyval = line.split(":", 2);
      String[] vals = keyval[1].split(",", 0);
      for (String val : vals) {
        values.add(val);
        String[] words = val.split("\\s+");
        if (maxSpan < words.length)
          maxSpan = words.length;
        for (String word : words)
          m.add(word);
        this.synonymMap.put(m, keyval[0]);
        m.clear();
      }

      obj = new HighlightObject(maxSpan, values);

      this.highlightObj.put(keyval[0], obj);
    }

    // System.out.println("high : "+this.highlightObj);
  }

  public InvertingReplacingSynonymFilterSource(String file, boolean replace) throws IOException, FileNotFoundException {
    this(file);

  }

  @Override
  public TokenFilter getTokenFilter(TokenStream tokenStream) {

    return new InvertingReplacingSynonymFilter(tokenStream, this.synonymMap, highlightObj);

  }

}
