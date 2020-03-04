package com.cleartrip.sw.search.context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.lucene.search.TimeLimitingCollector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.mrbean.MrBeanModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

import com.cleartrip.sw.search.util.Constants;

public class AppContextListener implements ServletContextListener {
  private static final Logger       log    = LoggerFactory
                                               .getLogger(AppContextListener.class);

  private static final ObjectMapper mapper = new ObjectMapper();
  static {
    mapper.registerModule(new MrBeanModule());
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    try {

      Properties prop = new Properties();
      ServletContext context = sce.getServletContext();

      prop.load(new FileInputStream(System
          .getProperty(Constants.ENVIRONMENT_FOLDER)
          + File.separator
          + Constants.ENVIRONMENT_FILE));

      ((LoggerContext) LoggerFactory.getILoggerFactory()).putProperty(
          Constants.USER_HOME_KEY, System.getProperty(Constants.USER_HOME_KEY));

      // JedisPool jedisPool = new JedisPool(new JedisPoolConfig(),
      // prop.getProperty(Constants.REDIS_HOST_KEY), Integer.parseInt(prop
      // .getProperty(Constants.REDIS_PORT_KEY)));

      ResourceManagerConfig config = mapper.readValue(
          new File(prop.getProperty(Constants.ETC_PATH_KEY) + File.separator
              + prop.getProperty(Constants.CONFIG_JSON_KEY)),
          ResourceManagerConfig.class);

      ResourceManager rm = config.deriveResourceManager(null, prop);
      context.setAttribute(Constants.CONFIG_JSON_KEY, rm);
      context.setAttribute(Constants.SYSTEM_PROPERTIES_KEY, prop);
      context.setAttribute(Constants.REQUEST_COUNTER_KEY, new AtomicLong(0l));
      // context.setAttribute(Constants.CACHE_POOL_KEY, jedisPool);
      log.debug("application context initialized");
    } catch (Exception e) {
      log.error("error ", e);
      // throw new RuntimeException(e);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    ServletContext context = sce.getServletContext();
    // JedisPool jedisPool = (JedisPool)
    // context.getAttribute(Constants.CACHE_POOL_KEY);
    // jedisPool.destroy();
    ((Properties) context.getAttribute(Constants.SYSTEM_PROPERTIES_KEY))
        .clear();
    ResourceManager resman = (ResourceManager) context
        .getAttribute(Constants.CONFIG_JSON_KEY);
    TimeLimitingCollector.getGlobalTimerThread().stopTimer();
    try {
      resman.destroy();
    } catch (IOException e) {
      log.debug("error " + e);
    }
    log.debug("application context destroyed");
  }

}
