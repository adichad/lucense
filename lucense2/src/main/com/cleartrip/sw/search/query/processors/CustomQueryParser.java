package com.cleartrip.sw.search.query.processors;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

import com.cleartrip.sw.search.query.QueryFactory;
import com.cleartrip.sw.search.searchj.SearchParameters;

public abstract class CustomQueryParser {
  protected final Analyzer     analyzer;
  protected final String       defaultField;
  protected final Version      version;
  protected final QueryFactory qf;

  public CustomQueryParser(Version version, Analyzer analyzer,
      String defaultField, QueryFactory qf) {
    this.analyzer = analyzer;
    this.defaultField = defaultField;
    this.version = version;
    this.qf = qf;
  }

  public abstract Query parse(SearchParameters searchParams)
      throws ParseException, IOException;

  public final Analyzer getAnalyzer() {
    return this.analyzer;
  }
}
