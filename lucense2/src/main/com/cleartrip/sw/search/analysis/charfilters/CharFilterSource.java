package com.cleartrip.sw.search.analysis.charfilters;

import java.util.Map;
import java.util.Properties;

import org.apache.lucene.analysis.CharFilter;
import org.apache.lucene.analysis.CharStream;

public abstract class CharFilterSource {
  public CharFilterSource(Map<String, ?> params, Properties env) {
    
  }
  public abstract CharFilter getCharFilter(CharStream stream) throws Exception;
}
