package com.cleartrip.sw.search.executors;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

public abstract class ExecuterServiceFactory {
  public ExecuterServiceFactory(Map<String, ?> params, Properties env) {

  }

  public abstract ExecutorService newExecutorService();
}
