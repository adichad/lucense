package com.cleartrip.sw.search.query.processors;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.TopFieldDocs;
import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.ExpressionCollector;
import com.adichad.lucense.expression.ExpressionFilteringCollectorFactory;
import com.adichad.lucense.expression.LucenseExpression;
import com.adichad.lucense.expression.ValueSources;
import com.cleartrip.sw.search.context.SortFieldFactory;
import com.cleartrip.sw.search.context.TaskStatus;
import com.cleartrip.sw.search.facets.FacetsJsonTopDocs;
import com.cleartrip.sw.search.facets.SearchFaceter;
import com.cleartrip.sw.search.facets.SearchFaceterFactory;
import com.cleartrip.sw.search.filters.SearchFilterFactory;
import com.cleartrip.sw.search.map.ViewPort;
import com.cleartrip.sw.search.searchj.CustomFieldSelector;
import com.cleartrip.sw.search.searchj.SearchParameters;
import com.cleartrip.sw.search.searchj.SearchResult;
import com.cleartrip.sw.search.sort.GeoDistanceComparatorSource;

public class URLQueryProcessor extends QueryProcessor {

  public URLQueryProcessor(Map<String, QueryParserFactory> myParsers,
      QueryParserFactory qParser,
      Map<String, List<SortFieldFactory>> mySorters,
      List<SortFieldFactory> defaultSorterChain, QueryProcessor catchProcessor,
      CustomFieldSelector fieldSelector,
      Map<String, CustomFieldSelector> myFieldSelectors,
      Map<String, SearchFaceterFactory> faceters,
      List<SearchFilterFactory> filters,
      Map<String, List<ExpressionFilteringCollectorFactory>> exprFilters,
      Set<String> scoreFields, Map<String, ?> params, Properties env) {
    super(myParsers, qParser, mySorters, defaultSorterChain, catchProcessor,
        fieldSelector, myFieldSelectors, faceters, filters, exprFilters,
        scoreFields, params, env);

  }

  @Override
  public SearchResult process(SearchParameters params, IndexSearcher searcher,
      TaskStatus log) throws Exception {
    SearchResult result = new SearchResult();
    CDSearchParameters searchParams = (CDSearchParameters) params;

    Map<String, Object2IntOpenHashMap<String>> externalValSource = new HashMap<>();
    Map<String, LucenseExpression> namedExprs = new HashMap<>();
    ValueSources valueSources = new ValueSources();
    searchParams.searcher = searcher;
    LinkedList<SortField> sorters = new LinkedList<SortField>();
    // List<SortFieldFactory> mainSorterFactories = sorterFactories.get("main");
    for (SortFieldFactory sorterFactory : searchParams.sorterChain) {
      sorters.add(sorterFactory.getSortField(externalValSource, namedExprs,
          valueSources, scoreFields));
    }

    if (searchParams.queryParams.containsKey("lat_ce_small_world")
        && searchParams.queryParams.containsKey("long_ce_small_world")) {
      sorters.addFirst(new SortField("geo",
          new GeoDistanceComparatorSource("lat_ce_small_world",
              "long_ce_small_world", Double
                  .parseDouble(searchParams.queryParams
                      .get("lat_ce_small_world")[0]), Double
                  .parseDouble(searchParams.queryParams
                      .get("long_ce_small_world")[0]), new double[] { 3, 10,
                  20, 40, 80 })));
    }
    Context cx = Context.enter();
    Sort sort = new Sort(sorters.toArray(new SortField[sorters.size()]));
    TopFieldCollector tfc = TopFieldCollector.create(sort, searchParams.offset
        + searchParams.limit, true, true, false, true);
    Collector c = tfc;
    Map<String, SearchFaceter> faceterMap = null;
    if (faceters != null && searchParams.enableFacets) {
      faceterMap = new HashMap<>(faceters.size());

      for (String faceterName : this.faceters.keySet()) {
        SearchFaceterFactory faceter = this.faceters.get(faceterName);
        c = faceter
            .createFaceter(c)
            .setSort(sort)
            .setFieldSelector(searchParams.fieldSelector)
            .setOffset(searchParams.facetOffset)
            .setLimit(searchParams.facetLimit)
            .setExpressionStuff(externalValSource, namedExprs, valueSources,
                scoreFields, cx).setViewPort(searchParams.viewPort);
        faceterMap.put(faceterName, (SearchFaceter) c);
      }
    }

    try {
      Map<String, ExpressionCollector> expressionCollectors = new HashMap<>();
      if (searchParams.enableResults)
        c = searchParams.fieldSelector.getExpressionCollectors(c,
            externalValSource, namedExprs, valueSources, scoreFields,
            expressionCollectors, cx);
      if (searchParams.filterChain != null) {
        for (ExpressionFilteringCollectorFactory efcf : searchParams.filterChain) {
          c = efcf.getCollector(c, externalValSource, namedExprs, valueSources,
              scoreFields, cx);
        }
      }
      Query query = this.createQuery(searchParams);
      log.info.append(query);
      searcher.search(query, null, c);
      TopFieldDocs tfd = (TopFieldDocs) tfc.topDocs(searchParams.offset,
          searchParams.limit);

      StringBuilder sb = new StringBuilder();
      sb.append("{");

      sb.append("\"total_count\":").append(tfd.totalHits).append(",");
      sb.append("\"result_count\":").append(tfd.scoreDocs.length);
      if (searchParams.enableResults) {
        sb.append(",").append("\"results\": [\n");
        int i = 0;
        for (ScoreDoc sd : tfd.scoreDocs) {
          Document sdoc = searcher.doc(sd.doc, searchParams.fieldSelector);
          sdoc = searchParams.fieldSelector.fillAuxFields(searcher, query,
              sdoc, sd, tfd);
          sdoc = searchParams.fieldSelector.fillExpressionValues(
              expressionCollectors, sdoc, sd);
          if (i != 0)
            sb.append(", ");
          searchParams.fieldSelector.decantAsJson(sdoc, sb, null, searcher);
          i = 1;
        }
        sb.append("]");
      }
      if (this.faceters != null && searchParams.enableFacets) {
        sb.append(", \"facets\":{ ");
        for (String facetName : faceterMap.keySet()) {
          sb.append("\"").append(facetName).append("\":")
              .append(faceterMap.get(facetName).getFacetJson(searcher, new FacetsJsonTopDocs()))
              .append(",");
        }
        sb.deleteCharAt(sb.length() - 1).append("}");
      }
      sb.append("\n}");
      result.setDocumentJson(sb.toString());
    } finally {
      Context.exit();
    }
    return result;
  }

  public static class CDSearchParameters extends SearchParameters {
    public boolean                                   enableResults   = true;
    public boolean                                   enableFacets    = false;
    public int                                       facetOffset     = 0;
    public int                                       facetLimit      = 20;
    public int                                       offset          = 0;
    public int                                       limit           = 100;
    public boolean                                   makeFuzzy       = false;
    public float                                     fuzzySimilarity = 0.5f;
    public Map<String, String[]>                     queryParams     = new HashMap<>();
    public boolean                                   scoreSignals    = false;
    public CustomFieldSelector                       fieldSelector;
    public IndexSearcher                             searcher        = null;
    public List<SortFieldFactory>                    sorterChain     = null;
    public List<ExpressionFilteringCollectorFactory> filterChain     = null;
    public ViewPort                                  viewPort        = new ViewPort(
                                                                         90,
                                                                         -180,
                                                                         -90,
                                                                         180, 0);

    public SearchParameters clone() {
      CDSearchParameters sp = new CDSearchParameters();
      sp.offset = offset;
      sp.limit = limit;
      sp.enableFacets = enableFacets;
      sp.facetOffset = facetOffset;
      sp.facetLimit = facetLimit;
      sp.makeFuzzy = makeFuzzy;
      sp.fuzzySimilarity = sp.fuzzySimilarity;
      sp.queryParams = queryParams;
      sp.scoreSignals = scoreSignals;
      sp.fieldSelector = fieldSelector;
      sp.searcher = searcher;
      sp.filterChain = filterChain;
      sp.viewPort = new ViewPort(viewPort.north, viewPort.west, viewPort.south,
          viewPort.east, viewPort.zoom);
      return sp;
    }
  }

  @Override
  public SearchParameters createSearchParams(Map<String, String[]> params,
      Reader reader) {
    CDSearchParameters searchParams = new CDSearchParameters();

    for (String name : params.keySet()) {
      String[] val = params.get(name);
      switch (name) {
      case "offset": {
        searchParams.offset = val == null ? 0 : Integer.parseInt(val[0]);
        break;
      }
      case "limit": {
        searchParams.limit = val == null ? 0 : Integer.parseInt(val[0]);
        break;
      }
      case "results": {
        if (val != null && val[0].equalsIgnoreCase("false")) {
          searchParams.enableResults = false;
        }
        break;
      }
      case "facets": {
        if (val != null && val[0].equalsIgnoreCase("true")) {
          searchParams.enableFacets = true;
        }
        break;
      }
      case "foffset":
      case "foff": {
        searchParams.facetOffset = val == null ? 0 : Integer.parseInt(val[0]);
        break;
      }
      case "flimit":
      case "flim": {
        searchParams.facetLimit = val == null ? 0 : Integer.parseInt(val[0]);
        break;
      }
      case "tt": {
        if (val != null && val[0].equalsIgnoreCase("fuzzy")) {
          searchParams.makeFuzzy = true;
        }
        break;
      }
      case "signals": {
        if (val != null && val[0].equalsIgnoreCase("true")) {
          searchParams.scoreSignals = true;
        }
        break;
      }
      case "fs": {
        if (val != null) {
          searchParams.fuzzySimilarity = Float.parseFloat(val[0]);
        }
        break;
      }
      case "flds": {
        if (val != null) {
          searchParams.fieldSelector = fieldSelectors.get(val[0]);
        }
        break;
      }
      case "sc": {
        if (val != null) {
          searchParams.sorterChain = this.sorterFactories.get(val[0]);
        }
        break;
      }
      case "fc": {
        if (val != null) {
          searchParams.filterChain = this.exprFilters.get(val[0]);
        }
        break;
      }
      case "n": {
        if (val != null) {
          searchParams.viewPort.north = Double.parseDouble(val[0]);
        }
        break;
      }
      case "w": {
        if (val != null) {
          searchParams.viewPort.west = Double.parseDouble(val[0]);
        }
        break;
      }
      case "s": {
        if (val != null) {
          searchParams.viewPort.south = Double.parseDouble(val[0]);
        }
        break;
      }
      case "e": {
        if (val != null) {
          searchParams.viewPort.east = Double.parseDouble(val[0]);
        }
        break;
      }
      case "z": {
        if (val != null) {
          searchParams.viewPort.zoom = Integer.parseInt(val[0]);
        }
        break;
      }

      default: {
        searchParams.queryParams.put(name, val);
        break;
      }
      }
    }
    if (searchParams.fieldSelector == null)
      searchParams.fieldSelector = defaultFieldSelector;
    if (searchParams.sorterChain == null)
      searchParams.sorterChain = defaultSorterChain;
    return searchParams;

  }

  @Override
  public Query createQuery(SearchParameters params) throws ParseException,
      IOException {
    return parser.parse(params);
  }

}
