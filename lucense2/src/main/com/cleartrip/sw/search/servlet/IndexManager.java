package com.cleartrip.sw.search.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.perf4j.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cleartrip.sw.search.context.ResourceManager;
import com.cleartrip.sw.search.context.TaskStatus;
import com.cleartrip.sw.search.data.DataManager;
import com.cleartrip.sw.search.util.Constants;

/**
 * Servlet implementation class IndexManager
 */
@WebServlet(description = "CRUD API for Search Engine", urlPatterns = { "/index/*" })
public class IndexManager extends HttpServlet {
  private static final long               serialVersionUID = 1L;
  private static final Logger             log              = LoggerFactory
                                                               .getLogger(IndexManager.class);
  private static final RestResourceMapper resourceMapper   = new IndexerParamsMapper();

  protected final void doPost(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    StopWatch timer = (StopWatch) request.getAttribute(Constants.TIMER_KEY);
    Long id = (Long) request.getAttribute(Constants.REQUEST_ID_KEY);
    ServletContext context = request.getServletContext();

    ResourceManager resources = (ResourceManager) context
        .getAttribute(Constants.CONFIG_JSON_KEY);

    Reader reader = new BufferedReader(new InputStreamReader(
        request.getInputStream(), "UTF-8"));
    Writer w = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
    resourceMapper.mapToAttributes(request, resources);

    Boolean commit = Boolean.parseBoolean(request.getParameter("commit"));
    Boolean purgeDeletes = Boolean.parseBoolean(request
        .getParameter("purgeDeletes"));
    Map<String, Object> params = new HashMap<String, Object>();
    params.put("commit", commit);
    params.put("purgeDeletes", purgeDeletes);
    TaskStatus innerLog = new TaskStatus();
    String requestType = request.getServletPath() + request.getPathInfo();
    request.setAttribute(Constants.REQUEST_GROUP_KEY, requestType);
    try {
      long rtgen = ((DataManager) request
          .getAttribute(Constants.DATA_MANAGER_KEY)).upsert(reader, innerLog,
          params);
      w.append("{").append("\"rtgen\"").append(":")
          .append(new Long(rtgen).toString()).append("}");
      log.info(
          "[{}] [{}] [{} ms] {}",
          new Object[] { requestType, id, timer.getElapsedTime(), innerLog.info });
    } catch (Exception e) {
      log.error("[{}] [{}] [{} ms] {}",
          new Object[] { requestType, id, timer.getElapsedTime(),
              innerLog.error, e });
    } finally {
      reader.close();
      w.flush();
    }
  }

  protected final void doPut(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    StopWatch timer = (StopWatch) request.getAttribute(Constants.TIMER_KEY);
    Long id = (Long) request.getAttribute(Constants.REQUEST_ID_KEY);
    ServletContext context = request.getServletContext();

    ResourceManager resources = (ResourceManager) context
        .getAttribute(Constants.CONFIG_JSON_KEY);
    Reader reader = new BufferedReader(new InputStreamReader(
        request.getInputStream(), "UTF-8"));

    resourceMapper.mapToAttributes(request, resources);
    boolean commit = Boolean.parseBoolean(request.getParameter("commit"));
    TaskStatus innerLog = new TaskStatus();
    String requestType = request.getServletPath() + request.getPathInfo();
    request.setAttribute(Constants.REQUEST_GROUP_KEY, requestType);
    try {
      ((DataManager) request.getAttribute(Constants.DATA_MANAGER_KEY)).insert(
          reader, innerLog, commit);
      log.info(
          "[{}] [{}] [{} ms] {}",
          new Object[] { requestType, id, timer.getElapsedTime(), innerLog.info });
    } catch (Exception e) {
      log.error("[{}] [{}] [{} ms] {}",
          new Object[] { requestType, id, timer.getElapsedTime(),
              innerLog.error, e });
    } finally {
      reader.close();
    }
  }

  protected final void doDelete(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    StopWatch timer = (StopWatch) request.getAttribute(Constants.TIMER_KEY);
    Long id = (Long) request.getAttribute(Constants.REQUEST_ID_KEY);
    ServletContext context = request.getServletContext();

    ResourceManager resources = (ResourceManager) context
        .getAttribute(Constants.CONFIG_JSON_KEY);

    resourceMapper.mapToAttributes(request, resources);
    boolean commit = Boolean.parseBoolean(request.getParameter("commit"));
    TaskStatus innerLog = new TaskStatus();
    String requestType = request.getServletPath() + request.getPathInfo();
    request.setAttribute(Constants.REQUEST_GROUP_KEY, requestType);
    try {
      ((DataManager) request.getAttribute(Constants.DATA_MANAGER_KEY)).delete(
          request.getParameterValues("qs"), innerLog, commit);
      log.info(
          "[{}] [{}] [{} ms] {}",
          new Object[] { requestType, id, timer.getElapsedTime(), innerLog.info });
    } catch (Exception e) {
      log.error("[{}] [{}] [{} ms] {}",
          new Object[] { requestType, id, timer.getElapsedTime(),
              innerLog.error, e });
    }
  }
}
