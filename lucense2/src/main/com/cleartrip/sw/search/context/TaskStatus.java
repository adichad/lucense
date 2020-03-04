package com.cleartrip.sw.search.context;

import java.util.Map;

public class TaskStatus {
  static enum Level {
    ERROR, WARN, INFO
  }
  private Level status;
  private Map<String, ?> map;
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(status).append(": ").append(map);
    return sb.toString();
  }
  
  public String toJSON() {
    return null;
  }

  public final StringBuilder error = new StringBuilder();
  public final StringBuilder warn  = new StringBuilder();
  public final StringBuilder info  = new StringBuilder();
  public final StringBuilder debug = new StringBuilder();
  public final StringBuilder trace = new StringBuilder();
}
