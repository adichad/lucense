package com.cleartrip.sw.search.query.processors;

import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.Version;

import com.cleartrip.sw.search.query.QueryFactory;


public abstract class QueryParserFactory {
  protected final QueryFactory qf;
  public QueryParserFactory(Version version, Analyzer an, Map<String, ?> params, QueryFactory qf) {
    this.qf = qf;
  }
  public abstract CustomQueryParser queryParser();
}
