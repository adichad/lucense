package com.cleartrip.sw.suggest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.IndexSearcherWrapper;

import com.cleartrip.sw.search.context.SortFieldFactory;
import com.cleartrip.sw.search.context.TaskStatus;

public class SuggestionProcessor {

  private static final String       SPACE                   = " ";
  private static final String       PLACE_TYPES             = "place_types_whole";
  private static final String       NAME                    = "name_whole";
  private static final String       ALIASES                 = "aliases_whole";
  private static final String       OPERATOR                = "operator";
  private List<String>              fieldsToSearch          = new ArrayList<String>();
  private List<String>              afterPartFieldsToSearch = new ArrayList<String>();

  public Boolean                    checkPlaceType          = true;
  private final SuggestionCollector collector;

  public SuggestionProcessor(SuggestionCollector collector) {
    this.collector = collector;
  }

  public SuggestResult process(Map<String, String[]> params, IndexSearcher s,
      TaskStatus log) throws Exception {
    String query = params.get("q")[0];
    if (query.endsWith(" "))
    	query = query.trim();
    query = query.toLowerCase();
    int offset = 0;
    int limit = 15;
    String[] temp;
    if ((temp = params.get("offset")) != null && temp.length > 0) {
      offset = Integer.parseInt(temp[0]);
    }
    if ((temp = params.get("limit")) != null && temp.length > 0) {
      limit = Integer.parseInt(temp[0]);
    }
    IndexSearcherWrapper searcher = (IndexSearcherWrapper) s;
    return collector.collect(query, searcher, offset, limit);
  }
}
