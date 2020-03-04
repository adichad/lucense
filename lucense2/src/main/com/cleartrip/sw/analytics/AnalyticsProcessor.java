package com.cleartrip.sw.analytics;

import java.io.Reader;
import java.util.Map;
import java.util.Properties;

import org.apache.lucene.search.IndexSearcher;

import com.cleartrip.sw.params.AnalyticsParams;
import com.cleartrip.sw.result.AnalyzeResult;
import com.cleartrip.sw.search.context.TaskStatus;

public abstract class AnalyticsProcessor {
  
  protected final Map<String, ?> configParams;
  protected final Properties env;

  public AnalyticsProcessor(Map<String, ?> params, Properties env) {
    this.configParams = params;
    this.env = env;
  }

  public abstract AnalyzeResult process(AnalyticsParams searchParams,
      IndexSearcher searcher, TaskStatus log) throws Exception;

  public abstract AnalyticsParams createParams(Map<String, String[]> qParams,
      Reader reader);
}
