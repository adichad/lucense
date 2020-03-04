package com.cleartrip.sw.search.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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
import com.cleartrip.sw.search.data.MongoDataMapper;
import com.cleartrip.sw.search.data.MongoIndexerFactory;
import com.cleartrip.sw.search.util.Constants;

/**
 * Servlet implementation class IndexManager
 */
@WebServlet(description = "CRUD API for Search Engine", urlPatterns = { "/index-bg" })
public class MongoIndexingService extends HttpServlet {
  private static final long               serialVersionUID = 1L;
  private static final Logger             log              = LoggerFactory
                                                               .getLogger(MongoIndexingService.class);
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
    
    String host = (String) request.getParameter("host");
    String db = (String) request.getParameter("db");
    String cn = (String) request.getParameter("cn");
    System.out.println("Host: " + host + ", DB Name: " + db + ", Collection: " + cn);
    Map<String, Object> params = new HashMap<>();
    params.put("batchSize", new Integer(10000));
    params.put("mongoHost", host);
    params.put("mongoPort", 27017);
    params.put("dbName", db);
    params.put("collectionName", cn);
    params.put("idField", "_id");
    params.put("commitInterval", 20);
    
    new Thread(new MongoIndexerFactory(params, null, resources.dataWriter("swplaces"), new MongoDataMapper()).createInstance()).start();
    response.getWriter().println("indexing process started.");
    
  }

  
}
