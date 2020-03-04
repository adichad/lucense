package com.cleartrip.sw.analytics;

import java.io.Reader;
import java.util.Map;
import java.util.Properties;

import org.apache.lucene.search.IndexSearcher;

import com.cleartrip.sw.params.AnalyticsParams;
import com.cleartrip.sw.result.AnalyzeResult;
import com.cleartrip.sw.search.context.TaskStatus;

public class StatisticsAnalyzer extends AnalyticsProcessor {

  public StatisticsAnalyzer(Map<String, ?> params, Properties env) {
    super(params, env);
    
  }

  @Override
  public AnalyzeResult process(AnalyticsParams params,
      IndexSearcher searcher, TaskStatus log) throws Exception {
    StringBuilder sb = new StringBuilder("work in progress");
    return new AnalyzeResult(sb);
  }

  @Override
  public AnalyticsParams createParams(Map<String, String[]> qParams,
      Reader reader) {
    // TODO Auto-generated method stub
    return null;
  }

}
