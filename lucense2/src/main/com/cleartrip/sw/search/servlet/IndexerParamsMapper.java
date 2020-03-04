package com.cleartrip.sw.search.servlet;

import javax.servlet.http.HttpServletRequest;

import com.cleartrip.sw.search.context.ResourceManager;

public class IndexerParamsMapper extends RestResourceMapper {

  @Override
  public void mapToAttributes(HttpServletRequest request, ResourceManager resources) {
    String[] restPath = request.getPathInfo().split("/");
    for(String part: restPath) {
      if(part!=null) {
        part = part.trim();
        if(!part.isEmpty()) {
          request.setAttribute("dataManager", resources.dataWriter(part));
          break;
        }
      }
    }
  }
}
