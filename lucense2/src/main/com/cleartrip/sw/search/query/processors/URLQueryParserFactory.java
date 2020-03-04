package com.cleartrip.sw.search.query.processors;

import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.Version;

import com.cleartrip.sw.search.query.QueryFactory;

public class URLQueryParserFactory extends QueryParserFactory {

  private Version version;
  private String defaultField;
  private Analyzer analyzer;

  public URLQueryParserFactory(Version version, Analyzer an, Map<String, ?> params, QueryFactory qf) {
    super(version, an, params, qf);
    this.version = version;
    this.defaultField = (String)params.get("defaultField");
    this.analyzer = an;
  }

  @Override
  public CustomQueryParser queryParser() {
    return new URLQueryParser(version, analyzer, defaultField, qf);
  }

}
