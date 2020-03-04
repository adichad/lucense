package com.cleartrip.sw.search.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.perf4j.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cleartrip.sw.search.context.ResourceManager;
import com.cleartrip.sw.search.context.TaskStatus;
import com.cleartrip.sw.search.util.Constants;

/**
 * Servlet implementation class IndexManager
 */
@WebServlet(description = "Search API", urlPatterns = { "/tokenize/*" })
public class Tokenizer extends HttpServlet {
  private static final long               serialVersionUID = 1L;
  private static final Logger             log              = LoggerFactory
                                                               .getLogger(Tokenizer.class);
  //private static final RestResourceMapper resourceMapper   = new IndexerParamsMapper();

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
      try {
        innerLog = new TaskStatus();
        id = (long) request.getAttribute(Constants.REQUEST_ID_KEY);
        context = request.getServletContext();
        resources = (ResourceManager) context
            .getAttribute(Constants.CONFIG_JSON_KEY);

        reader = new BufferedReader(new InputStreamReader(
            request.getInputStream(), "UTF-8"));

        // resourceMapper.mapToAttributes(request, resources);

        requestType = request.getServletPath() + request.getPathInfo();
        request.setAttribute(Constants.REQUEST_GROUP_KEY, requestType);

        w = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
        paramMap = new HashMap<>(request.getParameterMap());
        String[] analyzerNames = paramMap.get("analyzer");
        String[] texts = paramMap.get("text");
        String[] fields = paramMap.get("field");

        Analyzer an = resources.analyzer(analyzerNames[0]);
        Reader textReader = new StringReader(texts[0]);
        TokenStream tokStream = an.tokenStream(fields[0], textReader);
        CharTermAttribute termAtt = tokStream
            .addAttribute(CharTermAttribute.class);
        tokStream.reset();
        w.append("{ \"").append(analyzerNames[0]).append("\": [");
        int i = 0;
        while (tokStream.incrementToken()) {
          if (i++ > 0) {
            w.append(", ");
          }
          w.append("\"").append(termAtt.toString()).append("\"");
        }

        w.append("] }");
        tokStream.close();

        log.info("[{}] [{}] [{} ms] {} [{}] [{}{}{}]", new Object[] {
            requestType, id, timer.getElapsedTime(), innerLog.info,
            request.getRemoteAddr(), request.getRequestURL(), "?",
            request.getQueryString() });

      } catch (Exception e) {
        log.error(
            "[{}] [{}] [{} ms] [{}] {} [{}] [{}{}{}]",
            new Object[] { requestType, id, timer.getElapsedTime(),
                innerLog != null ? innerLog.info : "null",
                innerLog != null ? innerLog.error : "null",
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
