package com.cleartrip.sw.search.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cleartrip.sw.search.context.ResourceManager;

public abstract class RequestHandler {
  protected final ServletConfig servletConfig;
  protected final ServletContext servletContext;
  protected final ResourceManager resourceManager;

  public RequestHandler(ServletConfig config, ServletContext context) {
    this.servletConfig = config;
    this.servletContext = context;
    this.resourceManager = (ResourceManager)context.getAttribute("config.base");
  }
  
  public abstract void handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
