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
import com.cleartrip.sw.search.searchj.SearchResult;
import com.cleartrip.sw.search.util.Constants;

/**
 * Servlet implementation class IndexManager
 */
@WebServlet(description = "Search API", urlPatterns = { "/search/*" })
public class Searcher extends HttpServlet {
  private static final long               serialVersionUID = 1L;
  private static final Logger             log              = LoggerFactory
                                                               .getLogger(Searcher.class);
  private static final RestResourceMapper resourceMapper   = new IndexerParamsMapper();

  protected final void doGet(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    try {
      StopWatch timer = (StopWatch) request.getAttribute(Constants.TIMER_KEY);
      TaskStatus innerLog = null;
      long id = -1;
      Writer w = null;
      ServletContext context = null;
      ResourceManager resources = null;
      Reader reader = null;
      String requestType = null;
      Map<String, String[]> paramMap = null;
      String[] comments = null;
      try {
        innerLog = new TaskStatus();
        id = (long) request.getAttribute(Constants.REQUEST_ID_KEY);
        context = request.getServletContext();
        resources = (ResourceManager) context
            .getAttribute(Constants.CONFIG_JSON_KEY);

        reader = new BufferedReader(new InputStreamReader(
            request.getInputStream(), "UTF-8"));

        resourceMapper.mapToAttributes(request, resources);

        requestType = request.getServletPath() + request.getPathInfo();
        request.setAttribute(Constants.REQUEST_GROUP_KEY, requestType);

        w = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
        paramMap = new HashMap<>(request.getParameterMap());
        comments = paramMap.remove("comment");
        if (comments == null || comments.length == 0)
          comments = new String[] { "" };

        SearchResult result = ((DataManager) request
            .getAttribute(Constants.DATA_MANAGER_KEY)).search(paramMap, reader,
            innerLog);
        result.writeAsJsonTo(w);
        // result.writeAsJsonTo(response.getWriter());

        log.info("[{}] [{}] [{} ms] {} /* {} */ [{}] [{}{}{}]", new Object[] {
            requestType, id, timer.getElapsedTime(), innerLog.info,
            comments[0], request.getRemoteAddr(), request.getRequestURL(), "?",
            request.getQueryString() });

      } catch (Exception e) {
        log.error(
            "[{}] [{}] [{} ms] [{}] {} /* {} */ [{}] [{}{}{}]",
            new Object[] { requestType, id, timer.getElapsedTime(),
                innerLog != null ? innerLog.info : "null",
                innerLog != null ? innerLog.error : "null", comments[0],
                request.getRemoteAddr(), request.getRequestURL(), "?",
                request.getQueryString(), e });
        response.sendError(500, e.toString());

      } finally {
        w.flush();
      }
    } catch (Throwable e) {
      log.error("Exception on Flush or Error [{}] [{}{}{}]",
          new Object[] { request.getRemoteAddr(), request.getRequestURL(), "?",
              request.getQueryString(), e });
      throw e;
    }

  }

}
