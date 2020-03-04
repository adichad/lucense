package com.cleartrip.sw.search.query.processors;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import com.adichad.lucense.expression.ExpressionFilteringCollectorFactory;
import com.cleartrip.sw.search.context.SortFieldFactory;
import com.cleartrip.sw.search.context.TaskStatus;
import com.cleartrip.sw.search.facets.SearchFaceterFactory;
import com.cleartrip.sw.search.filters.SearchFilterFactory;
import com.cleartrip.sw.search.searchj.CustomFieldSelector;
import com.cleartrip.sw.search.searchj.SearchParameters;
import com.cleartrip.sw.search.searchj.SearchResult;

public abstract class QueryProcessor {

  protected final CustomQueryParser                                      parser;
  protected final Map<String, CustomQueryParser>                         myParsers;
  protected final QueryProcessor                                         catchProcessor;
  protected final CustomFieldSelector                                    defaultFieldSelector;
  protected final List<SearchFilterFactory>                              filters;
  protected final Map<String, SearchFaceterFactory>                      faceters;
  protected final long                                                   searchTimeout;
  protected final Map<String, List<SortFieldFactory>>                    sorterFactories;
  protected final Map<String, List<ExpressionFilteringCollectorFactory>> exprFilters;
  protected final Set<String>                                            scoreFields;
  protected final Map<String, CustomFieldSelector>                       fieldSelectors;
  protected final List<SortFieldFactory>                                 defaultSorterChain;

  public QueryProcessor(Map<String, QueryParserFactory> myParsers, QueryParserFactory qParser,
      Map<String, List<SortFieldFactory>> mySorters,
      List<SortFieldFactory> defaultSorterChain, QueryProcessor catchProcessor,
      CustomFieldSelector defaultFieldSelector,
      Map<String, CustomFieldSelector> myFieldSelectors,
      Map<String, SearchFaceterFactory> faceters,
      List<SearchFilterFactory> filters,
      Map<String, List<ExpressionFilteringCollectorFactory>> exprFilters,
      Set<String> scoreFields, Map<String, ?> params, Properties env) {
    this.parser = qParser.queryParser();
    this.myParsers = new HashMap<String, CustomQueryParser>(myParsers.size());
    for(String name: myParsers.keySet()) {
      this.myParsers.put(name, myParsers.get(name).queryParser());
    }
    this.defaultSorterChain = defaultSorterChain;
    this.sorterFactories = mySorters;
    this.catchProcessor = catchProcessor;
    this.defaultFieldSelector = defaultFieldSelector;
    this.fieldSelectors = myFieldSelectors;
    this.faceters = faceters;
    this.filters = filters;
    this.exprFilters = exprFilters;
    this.scoreFields = scoreFields;
    String key = (String) params.get("searchTimeoutEnv");
    String timeout;
    if (key == null)
      timeout = "5000";
    else
      timeout = env.getProperty(key, "5000");
    this.searchTimeout = Long.parseLong(timeout);
  }

  public abstract SearchResult process(SearchParameters searchParams,
      IndexSearcher searcher, TaskStatus log) throws Exception;

  public abstract SearchParameters createSearchParams(
      Map<String, String[]> qParams, Reader reader);

  public abstract Query createQuery(SearchParameters searchParams)
      throws ParseException, IOException;

}
