package com.adichad.lucense.request.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.adichad.lucense.request.Request.FieldType;

public class SearchResultRow {
  private Map<String, FieldType> schema;

  private List<?> vals;

  public SearchResultRow(Map<String, FieldType> schema) {
    this.schema = schema;
    this.vals = new ArrayList<Object>(schema.size());
  }

  public void put(String field, Object val) {

  }

}
