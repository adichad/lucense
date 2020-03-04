package com.cleartrip.sw.search.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

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
 * Servlet implementation class Admin
 */
@WebServlet(description = "Admin tools API for Search Engine", urlPatterns = { "/admin/*" })
public class Admin extends HttpServlet {
  private static final long               serialVersionUID = 1L;
  private static final Logger             log              = LoggerFactory
                                                               .getLogger(Admin.class);
  private static final RestResourceMapper resourceMapper   = new IndexerParamsMapper();

  protected final void doGet(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    StopWatch timer = (StopWatch) request.getAttribute(Constants.TIMER_KEY);
    Long id = (Long) request.getAttribute(Constants.REQUEST_ID_KEY);
    ServletContext context = request.getServletContext();

    ResourceManager resources = (ResourceManager) context
        .getAttribute(Constants.CONFIG_JSON_KEY);

    Reader reader = new BufferedReader(new InputStreamReader(
        request.getInputStream(), "UTF-8"));
    //Writer w = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
    resourceMapper.mapToAttributes(request, resources);

    Boolean enableRT = Boolean.parseBoolean(request.getParameter("rt"));
    TaskStatus innerLog = new TaskStatus();
    String requestType = request.getServletPath() + request.getPathInfo();
    request.setAttribute(Constants.REQUEST_GROUP_KEY, requestType);
    try {
      ((DataManager) request
          .getAttribute(Constants.DATA_MANAGER_KEY)).toggleNRT(enableRT);
      log.info(
          "[{}] [{}] [{} ms] {}",
          new Object[] { requestType, id, timer.getElapsedTime(), innerLog.info });
    } catch (Exception e) {
      log.error("[{}] [{}] [{} ms] {}",
          new Object[] { requestType, id, timer.getElapsedTime(),
              innerLog.error, e });
    } finally {
      reader.close();
      //w.flush();
    }
  }
}
