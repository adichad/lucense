package com.cleartrip.sw.search.context;

import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;

import com.cleartrip.sw.search.util.Constants;

public class AppRequestListener implements ServletRequestListener {

  @Override
  public void requestInitialized(ServletRequestEvent arg0) {
    StopWatch timer = new Slf4JStopWatch();
    ServletRequest request = arg0.getServletRequest();
    //JedisPool jedisPool = (JedisPool) request.getServletContext().getAttribute(
    //    Constants.CACHE_POOL_KEY);
    //Jedis cache = jedisPool.getResource();
    try {
      
      long id = ((AtomicLong)request.getServletContext().getAttribute(Constants.REQUEST_COUNTER_KEY)).incrementAndGet();
      //long id = cache.incr(Constants.REDIS_REQUEST_ID_KEY);
      request.setAttribute(Constants.REQUEST_ID_KEY, id);
    } catch (Exception e) {
      e.printStackTrace();
    }
    //request.setAttribute(Constants.CACHE_KEY, cache);
    request.setAttribute(Constants.TIMER_KEY, timer);
  }

  @Override
  public void requestDestroyed(ServletRequestEvent arg0) {
    ServletRequest request = arg0.getServletRequest();
    /*
    Jedis cache = (Jedis) request.getAttribute(Constants.CACHE_KEY);
    if (cache != null) {
      JedisPool jedisPool = (JedisPool) request.getServletContext()
          .getAttribute(Constants.CACHE_POOL_KEY);
      jedisPool.returnResource(cache);
    }
    */
    StopWatch timer = (StopWatch)request.getAttribute(Constants.TIMER_KEY);
    timer.stop((String)request.getAttribute(Constants.REQUEST_GROUP_KEY));
  }

}
