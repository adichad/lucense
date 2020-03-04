package com.cleartrip.sw.search.analysis.charfilters;

import java.util.Map;
import java.util.Properties;

import org.apache.lucene.analysis.CharFilter;
import org.apache.lucene.analysis.CharStream;
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilter;

public class HTMLStripCharFilterSource extends CharFilterSource {

  public HTMLStripCharFilterSource(Map<String, ?> params, Properties env) {
    super(params, env);
  }

  @Override
  public CharFilter getCharFilter(CharStream stream) throws Exception {
    return new HTMLStripCharFilter(stream);
  }

}
