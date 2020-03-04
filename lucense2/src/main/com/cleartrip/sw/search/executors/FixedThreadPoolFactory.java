package com.cleartrip.sw.search.executors;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.lucene.util.NamedThreadFactory;


public class FixedThreadPoolFactory extends ExecuterServiceFactory {

  private int           maxThreadsPerSearch;
  private ThreadFactory threadFactory;

  public FixedThreadPoolFactory(Map<String, ?> params, Properties env) {
    super(params, env);
    this.maxThreadsPerSearch = (Integer) params.get("maxThreadsPerSearch");

    this.threadFactory = new NamedThreadFactory(
        (String) params.get("threadPrefix"));
  }

  @Override
  public ExecutorService newExecutorService() {
    return Executors.newFixedThreadPool(maxThreadsPerSearch, threadFactory);
  }
}
