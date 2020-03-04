package com.cleartrip.sw.search.servlet;

import javax.servlet.http.HttpServletRequest;

import com.cleartrip.sw.search.context.ResourceManager;

public abstract class RestResourceMapper {
  public abstract void mapToAttributes(HttpServletRequest request, ResourceManager resources);
}
