package com.cleartrip.sw.search.query.processors;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.doubles.DoubleComparators;
import it.unimi.dsi.fastutil.doubles.DoubleOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TimeLimitingCollector;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.util.Counter;
import org.codehaus.jackson.io.JsonStringEncoder;
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
import com.cleartrip.sw.search.query.processors.StrictNLPQueryParser.LatLong;
import com.cleartrip.sw.search.searchj.CustomFieldSelector;
import com.cleartrip.sw.search.searchj.SearchParameters;
import com.cleartrip.sw.search.searchj.SearchResult;
import com.cleartrip.sw.search.sort.GeoDistanceComparatorSource;

public class SuggestQueryProcessor extends QueryProcessor {

  // private final POSModel posModel;

  public SuggestQueryProcessor(Map<String, QueryParserFactory> myParsers,
      QueryParserFactory qParser,
      Map<String, List<SortFieldFactory>> mySorters,
      List<SortFieldFactory> defaultSorterChain, QueryProcessor catchProcessor,
      CustomFieldSelector fieldSelector,
      Map<String, CustomFieldSelector> myFieldSelectors,
      Map<String, SearchFaceterFactory> faceters,
      List<SearchFilterFactory> filters,
      Map<String, List<ExpressionFilteringCollectorFactory>> exprFilters,
      Set<String> scoreFields, Map<String, ?> params, Properties env)
      throws IOException {
    super(myParsers, qParser, mySorters, defaultSorterChain, catchProcessor,
        fieldSelector, myFieldSelectors, faceters, filters, exprFilters,
        scoreFields, params, env);

    /*
     * FileInputStream fin = null; try { fin = new
     * FileInputStream(env.getProperty(Constants.ETC_PATH_KEY) + File.separator
     * + (String) params.get("posTagModel")); this.posModel = new POSModel(fin);
     * } finally { if (fin != null) { fin.close(); fin = null; } }
     */
  }

  @Override
  public SearchResult process(SearchParameters params, IndexSearcher searcher,
      TaskStatus log) throws Exception {

    SearchResult searchResult = doSearch(params, searcher, log);

    if (searchResult.getTotalCount() == 0) {
      SuggestSearchParameters searchParams = (SuggestSearchParameters) params;
      String[] qparts;
      if ((qparts = searchParams.queryString
          .split("\\b(?:in|inside|within)\\b")).length > 1) {
        searchParams.queryString = StringUtils.join(qparts, " near ");
        searchResult = doSearch(params, searcher, log);
      }
    }

    return searchResult;
  }

  public SearchResult doSearch(SearchParameters params, IndexSearcher searcher,
      TaskStatus log) throws Exception {
    Counter clock = TimeLimitingCollector.getGlobalCounter();
    long baseline = clock.get();
    JsonStringEncoder encoder = JsonStringEncoder.getInstance();
    SearchResult result = new SearchResult();
    SuggestSearchParameters searchParams = (SuggestSearchParameters) params;
    int lim = searchParams.offset + searchParams.limit + searchParams.lookAhead;

    searchParams.searcher = searcher;
    // searchParams.posTagger = new POSTaggerME(this.posModel);
    Map<String, Object2IntOpenHashMap<String>> externalValSource = new HashMap<>();
    Map<String, LucenseExpression> namedExprs = new HashMap<>();
    ValueSources valueSources = new ValueSources();
    Context cx = Context.enter();
    try {

      List<SortFieldFactory> mainSorterFactories = sorterFactories
          .get(searchParams.mainSortName);
      LinkedList<SortField> sorters = new LinkedList<SortField>();
      for (SortFieldFactory sorterFactory : mainSorterFactories) {
        sorters.add(sorterFactory.getSortField(externalValSource, namedExprs,
            valueSources, scoreFields));
      }

      if (searchParams.geoSort
          && searchParams.queryParams.containsKey("lat_ce_small_world")
          && searchParams.queryParams.containsKey("long_ce_small_world")) {
        if (searchParams.geoSortBuckets == null) {
          if (searchParams.geoSortBucketInterval > 0.0d) {
            DoubleArrayList list = new DoubleArrayList();
            for (double d = 0.0d; d < searchParams.dist; d += searchParams.geoSortBucketInterval) {
              list.add(d);
            }
            searchParams.geoSortBuckets = list.toDoubleArray();
          } else {
            searchParams.geoSortBuckets = searchParams.defaultBuckets;
          }
        }
        sorters.addFirst(new SortField("geo", new GeoDistanceComparatorSource(
            "lat_ce_small_world", "long_ce_small_world",
            Double.parseDouble(searchParams.queryParams
                .get("lat_ce_small_world")[0]), Double
                .parseDouble(searchParams.queryParams
                    .get("long_ce_small_world")[0]),
            searchParams.geoSortBuckets)));
      }
      searchParams.sort = new Sort(
          sorters.toArray(new SortField[sorters.size()]));

      List<SortFieldFactory> nameSorterFactories = sorterFactories.get("name");
      List<SortField> nameSorters = new ArrayList<SortField>(
          nameSorterFactories.size());
      for (SortFieldFactory sorterFactory : nameSorterFactories) {
        nameSorters.add(sorterFactory.getSortField(externalValSource,
            namedExprs, valueSources, scoreFields));
      }
      searchParams.nameSort = new Sort(
          nameSorters.toArray(new SortField[nameSorters.size()]));

      ScoreDoc subdoc = null;
      Query geoquery = null;
      String originalQuery = searchParams.queryString;
      searchParams.queryString = searchParams.queryString.toLowerCase();
      if (searchParams.multiSearch) {
        Query restrictedQuery = null;
        String[] qparts = searchParams.queryString
            .split("\\b(?:near|nearby)\\b");
        if (qparts.length <= 1) {
          if (searchParams.queryString.split("\\b(?:in|inside|within)\\b").length <= 1) {
            // ///////////////////////////////////////////////
            restrictedQuery = this.createQuery(restrict(searchParams));
            // System.out.println("name query: " + restrictedQuery);

            TopFieldCollector subtfc = TopFieldCollector.create(
                searchParams.nameSort, 1, true, true, false, true);
            Collector subc = subtfc;
            List<ExpressionFilteringCollectorFactory> efils = exprFilters
                .get("name");

            for (ExpressionFilteringCollectorFactory efcf : efils) {
              subc = efcf.getCollector(subc, externalValSource, namedExprs,
                  valueSources, scoreFields, cx);
            }

            Map<String, ExpressionCollector> subExpressionCollectors = new HashMap<>();
            subc = searchParams.fieldSelector.getExpressionCollectors(subc,
                externalValSource, namedExprs, valueSources, scoreFields,
                subExpressionCollectors, cx);

            searcher.search(restrictedQuery, subc);

            ScoreDoc[] subsd = ((TopFieldDocs) subtfc.topDocs()).scoreDocs;
            if (subsd.length > 0) {
              subdoc = subsd[0];
              /*
               * for (ScoreDoc mysubdoc : subsd) { Document mydoc =
               * searcher.doc(mysubdoc.doc, searchParams.fieldSelector);
               * searchParams.fieldSelector.fillExpressionValues(
               * subExpressionCollectors, mydoc, mysubdoc);
               * 
               * System.out.println("doc: " + mysubdoc.doc + "[" +
               * mydoc.getFieldable("name").stringValue() + "(" +
               * mydoc.getFieldable("_id").stringValue() + ")=>" +
               * mydoc.getFieldable("nlpNameComparator").stringValue() + "(" +
               * "name:" + mydoc.getFieldable("isFullExactName").stringValue() +
               * "," + "aliases:" +
               * mydoc.getFieldable("isFullExactAliases").stringValue() + "," +
               * "name@geo:" +
               * mydoc.getFieldable("fullNameGeoMatch").stringValue() + "," +
               * ")" + "] restricted subquery: " + restrictedQuery);
               * 
               * }
               */
            }
          }
        } else {
          SuggestSearchParameters restrictedParams = (SuggestSearchParameters) restrict(
              searchParams, true);
          restrictedParams.queryString = qparts[1];
          restrictedQuery = this.createQuery(restrictedParams);
          TopFieldCollector subtfc = TopFieldCollector.create(
              searchParams.nameSort, 1, true, true, false, true);
          Collector subc = subtfc;
          for (ExpressionFilteringCollectorFactory efcf : exprFilters
              .get("name")) {
            subc = efcf.getCollector(subc, externalValSource, namedExprs,
                valueSources, scoreFields, cx);
          }

          /*
           * Map<String, ExpressionCollector> subExpressionCollectors = new
           * HashMap<>(); subc =
           * searchParams.fieldSelector.getExpressionCollectors(subc,
           * externalValSource, namedExprs, valueSources, scoreFields,
           * subExpressionCollectors, cx);
           */
          searcher.search(restrictedQuery, subc);
          ScoreDoc[] subsd = ((TopFieldDocs) subtfc.topDocs()).scoreDocs;
          if (subsd.length > 0) {
            // found doc for latlongs
            subdoc = subsd[0];
            // System.out.println("first: "
            // +FieldCache.DEFAULT.getStrings(searcher.getIndexReader(),
            // "name_as_id")[subdoc.doc]);
          } else {
            searchParams.queryString = qparts[1];
            Query q = this.createQuery(searchParams);
            subtfc = TopFieldCollector.create(searchParams.sort, 1, true, true,
                false, true);
            searcher.search(q, subtfc);
            subsd = ((TopFieldDocs) subtfc.topDocs()).scoreDocs;
            if (subsd.length > 0) {
              // found doc for latlongs
              subdoc = subsd[0];
              // System.out.println("second: "
              // +FieldCache.DEFAULT.getStrings(searcher.getIndexReader(),
              // "name_as_id")[subdoc.doc]);
            } else {
              subdoc = null;// ??????????????????????
            }
          }
          if (subdoc != null) {
            int doc = subdoc.doc;
            /*
             * System.out.println("doc: [" +
             * FieldCache.DEFAULT.getStrings(reader, "name_as_id")[doc] +
             * "] subquery: " + latlongGetter);
             */
            LatLong latLong = new LatLong();
            geoquery = ((StrictNLPQueryParser) searchParams.parser)
                .getGeoQuery(doc, searchParams.searcher.getIndexReader(),
                    latLong);
            double latitude = latLong.latitude;
            double longitude = latLong.longitude;
            LinkedList<SortField> geosorters = new LinkedList<>();

            for (SortField sf : searchParams.sort.getSort())
              geosorters.add(sf);

            geosorters.addFirst(new SortField("geo",
                new GeoDistanceComparatorSource("lat_ce_small_world",
                    "long_ce_small_world", latitude, longitude, new double[] {
                        3, 10, 20, 40, 80 })));
            searchParams.sort.setSort(geosorters
                .toArray(new SortField[geosorters.size()]));
            searchParams.queryString = qparts[0];

          }
        }
      }
      // ///////////////////////////////////////////////
      Query query = this.createQuery(searchParams);
      Collector c = null;
      TopFieldCollector tfc = null;
      if (searchParams.enableResults) {
        tfc = TopFieldCollector.create(searchParams.sort, lim, true, true,
            false, true);
        c = tfc;
      }

      Map<String, SearchFaceter> faceterMap = null;
      if (faceters != null && searchParams.enableFacets) {
        faceterMap = new HashMap<>(faceters.size());

        for (String faceterName : searchParams.facets) {
          SearchFaceterFactory faceter = this.faceters.get(faceterName);
          c = faceter
              .createFaceter(c)
              .setSort(searchParams.sort)
              .setFieldSelector(searchParams.fieldSelector)
              .setOffset(searchParams.facetOffset)
              .setLimit(searchParams.facetLimit)
              .setExpressionStuff(externalValSource, namedExprs, valueSources,
                  scoreFields, cx).setViewPort(searchParams.viewPort);
          faceterMap.put(faceterName, (SearchFaceter) c);
        }

      }

      if (geoquery != null) {
        Query fq = searchParams.parser.qf.newBooleanQuery(false);
        searchParams.parser.qf.add(fq, query, Occur.MUST);
        searchParams.parser.qf.add(fq, geoquery, Occur.MUST);
        query = fq;
        subdoc = null;
      }

      Map<String, ExpressionCollector> expressionCollectors = null;
      if (searchParams.enableResults) {
        expressionCollectors = new HashMap<>();
        c = searchParams.fieldSelector.getExpressionCollectors(c,
            externalValSource, namedExprs, valueSources, scoreFields,
            expressionCollectors, cx);
      }

      StringBuilder sb = new StringBuilder();
      sb.append("{");

      if (searchParams.enableResults || searchParams.enableFacets) {
        if (searchParams.filterChain != null) {
          for (ExpressionFilteringCollectorFactory efcf : searchParams.filterChain) {
            c = efcf.getCollector(c, externalValSource, namedExprs,
                valueSources, scoreFields, cx);
          }
        }

        TimeLimitingCollector tlc = new TimeLimitingCollector(c, clock,
            searchTimeout);
        tlc.setBaseline(baseline);
        tlc.setGreedy(true);
        c = tlc;
        sb.append("\"errors\": {\n");
        boolean needComma = false;

        try {
          long start = System.currentTimeMillis();
          TopDocs tds = searcher.search(query, 1);
          long time = System.currentTimeMillis() - start;
          if (tds.totalHits > 100000) {
            Query q = searchParams.parser.qf.newTermQuery(new Term(
                "place_type_small_world", "Other"), 0);
            searchParams.parser.qf.add(query, q, Occur.MUST_NOT);
            q = searchParams.parser.qf.newTermQuery(new Term(
                "place_type_small_world", "POI"), 0);
            searchParams.parser.qf.add(query, q, Occur.MUST_NOT);
            q = searchParams.parser.qf.newTermQuery(new Term(
                "place_type_small_world", "Stay"), 0);
            searchParams.parser.qf.add(query, q, Occur.MUST_NOT);
            q = searchParams.parser.qf.newTermQuery(new Term(
                "place_type_small_world", "Shopping"), 0);
            searchParams.parser.qf.add(query, q, Occur.MUST_NOT);
            q = searchParams.parser.qf.newTermQuery(new Term(
                "place_type_small_world", "Nightlife"), 0);
            searchParams.parser.qf.add(query, q, Occur.MUST_NOT);
            q = searchParams.parser.qf.newTermQuery(new Term(
                "place_type_small_world", "Activities"), 0);
            searchParams.parser.qf.add(query, q, Occur.MUST_NOT);
            q = searchParams.parser.qf.newTermQuery(new Term(
                "place_type_small_world", "Eat"), 0);
            searchParams.parser.qf.add(query, q, Occur.MUST_NOT);
            sb.append("\"found\": \"").append(tds.totalHits)
                .append(" docs in ").append(time)
                .append(" ms. filtering other place_types.\"");
            needComma = true;
          }
          searcher.search(query, null, c);
        } catch (TimeLimitingCollector.TimeExceededException e) {
          log.info.append("[timeout] ");
          if (needComma)
            sb.append(", ");
          sb.append("\"timeout\": \"")
              .append(encoder.quoteAsString(e.toString())).append("\"");
        }
        sb.append("}, ");
      }

      log.info.append("[").append(searchParams.offset).append("/")
          .append(searchParams.limit);
      log.info.append("/").append(searchParams.lookAhead).append("] ");
      // System.out.println(searchParams.sort);

      TopFieldDocs tfd = null;

      if (searchParams.enableResults) {

        if (subdoc == null) {
          tfd = (TopFieldDocs) tfc.topDocs(searchParams.offset,
              searchParams.limit);
          log.info.append("[").append(tfd.totalHits).append(" docs] ");

          sb.append("\"total_count\":").append(tfd.totalHits).append(",");
          sb.append("\"result_count\":").append(tfd.scoreDocs.length)
              .append(",");
          sb.append("\"results\": [\n");

          for (ScoreDoc sd : tfd.scoreDocs) {
            Document sdoc = searcher.doc(sd.doc, searchParams.fieldSelector);
            sdoc = searchParams.fieldSelector.fillAuxFields(searcher, query,
                sdoc, sd, tfd);
            sdoc = searchParams.fieldSelector.fillExpressionValues(
                expressionCollectors, sdoc, sd);
            searchParams.fieldSelector.decantAsJson(sdoc, sb, null, searcher);
            sb.append(",");
          }
        } else {
          tfd = (TopFieldDocs) tfc.topDocs(0, searchParams.offset
              + searchParams.limit);
          log.info.append("[").append(tfd.totalHits).append(" docs] ");

          sb.append("\"total_count\":").append(tfd.totalHits).append(",");
          sb.append("\"result_count\":")
              .append(
                  Math.min(tfd.scoreDocs.length - searchParams.offset,
                      searchParams.limit)).append(",");
          sb.append("\"results\": [\n");
          LinkedList<ScoreDoc> finalSds = new LinkedList<>();
          boolean filled = false;

          for (int i = 0; i < searchParams.offset; i++) {
            // subsd found on some previous page
            if (tfd.scoreDocs[i].doc == subdoc.doc) {
              int limit = Math.min(tfd.scoreDocs.length, searchParams.offset
                  + searchParams.limit);
              for (int j = searchParams.offset; j < limit; j++) {
                finalSds.add(tfd.scoreDocs[j]);
              }
              filled = true;
              break;
            }
          }
          if (!filled) {
            // subsd found on current page (or not, in which case fill and
            // delegate to next condition check
            int limit = Math.min(tfd.scoreDocs.length, searchParams.offset
                + searchParams.limit);
            for (int i = searchParams.offset; i < limit; i++) {
              if (tfd.scoreDocs[i].doc == subdoc.doc) {
                filled = true;
              } else
                finalSds.add(tfd.scoreDocs[i]);
            }
            if (filled)
              finalSds
                  .addFirst(searchParams.offset > 0 ? tfd.scoreDocs[searchParams.offset - 1]
                      : subdoc);
          }
          if (!filled) {
            if (searchParams.offset == 0) {
              finalSds.addFirst(subdoc);
            } else {
              finalSds.addFirst(tfd.scoreDocs[searchParams.offset - 1]);
            }
            finalSds.removeLast();
          }
          for (ScoreDoc sd : finalSds) {
            Document sdoc = searcher.doc(sd.doc, searchParams.fieldSelector);
            sdoc = searchParams.fieldSelector.fillAuxFields(searcher, query,
                sdoc, sd, tfd);
            sdoc = searchParams.fieldSelector.fillExpressionValues(
                expressionCollectors, sdoc, sd);
            searchParams.fieldSelector.decantAsJson(sdoc, sb, null, searcher);
            sb.append(",");
            /*
             * if (sd.doc == subdoc.doc) System.out.println("doc: " + subdoc.doc
             * + "[" + sdoc.getFieldable("name").stringValue() + "(" +
             * sdoc.getFieldable("_id").stringValue() + ")=>" +
             * sdoc.getFieldable("nlpNameComparator").stringValue() + "(" +
             * "name:" + sdoc.getFieldable("isFullExactName").stringValue() +
             * "," + "aliases:" +
             * sdoc.getFieldable("isFullExactAliases").stringValue() + "," +
             * "name@geo:" + sdoc.getFieldable("fullNameGeoMatch").stringValue()
             * + "," + ")");
             */

          }

        }
        if (tfd != null)
          result.setTotalCount(tfd.totalHits);
        sb.deleteCharAt(sb.length() - 1).append("]");
      }
      log.info.append("[").append(originalQuery).append("]");
      log.info.append("=>[").append(query).append("]");

      if (this.faceters != null && searchParams.enableFacets) {
        if (searchParams.enableResults)
          sb.append(", ");
        sb.append("\"facets\":{ ");
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

  public static class SuggestSearchParameters extends SearchParameters {
    public boolean                                   enableResults         = true;
    public boolean                                   enableFacets          = true;
    public int                                       facetLimit            = 20;
    public int                                       facetOffset           = 0;
    public boolean                                   multiSearch           = true;
    public Sort                                      nameSort              = null;
    public int                                       lookAhead             = 1;
    // public NameFinderME placeFinder = null;
    public int                                       offset                = 0;
    public int                                       limit                 = 100;
    public String                                    queryString           = "";
    public IndexSearcher                             searcher              = null;
    
    public Sort                                      sort                  = null;
    public Map<String, Query>                        facetFilters          = new HashMap<>();
    public Map<String, String[]>                     queryParams           = new HashMap<>();
    public boolean                                   dedup                 = true;
    public boolean                                   invertHidden          = false;
    public CustomFieldSelector                       fieldSelector         = null;
    public Occur                                     occur                 = Occur.MUST;
    public Occur                                     subOccur              = Occur.SHOULD;
    public HashSet<String>                           fields                = new HashSet<>();
    public CustomQueryParser                         parser;
    public int                                       fuzzyThreshold        = 10;
    public String                                    mainSortName          = "main";
    public boolean                                   makeFuzzy             = false;
    public float                                     fuzzySimilarity       = 0.0f;
    public ViewPort                                  viewPort              = new ViewPort(
                                                                               90,
                                                                               -180,
                                                                               -90,
                                                                               180,
                                                                               0);
    public boolean                                   geoSort               = true;
    public List<ExpressionFilteringCollectorFactory> filterChain           = null;
    public Set<String>                               facets                = null;
    public double                                    geoSortBucketInterval = 0.0d;
    public double[]                                  geoSortBuckets        = null;
    public final double[]                            defaultBuckets        = new double[] {
                                                                               3,
                                                                               10,
                                                                               20,
                                                                               40,
                                                                               80 };
    public double                                    dist;
    public String                                    coordinateQuery       = null;
    public String                                    latField              = "lat_ce_small_world";
    public String                                    longField             = "long_ce_small_world";

    public SuggestSearchParameters() {
      fields.add("name");
      fields.add("aliases");
      fields.add("geo_path_aliases");
      fields.add("place_type");
      fields.add("themes");
    }

    public SearchParameters clone() {
      SuggestSearchParameters sp = new SuggestSearchParameters();
      sp.enableResults = true;
      sp.enableFacets = false;
      sp.facetOffset = facetOffset;
      sp.facetLimit = facetLimit;
      sp.lookAhead = lookAhead;
      sp.offset = offset;
      sp.limit = limit;
      sp.queryString = queryString;
      sp.searcher = searcher;
      sp.sort = sort;
      sp.facetFilters = facetFilters;
      sp.dedup = dedup;
      sp.invertHidden = invertHidden;
      sp.fieldSelector = fieldSelector;
      sp.occur = occur;
      sp.subOccur = subOccur;
      sp.fields = fields;
      sp.multiSearch = multiSearch;
      sp.parser = parser;
      sp.fuzzyThreshold = fuzzyThreshold;
      sp.viewPort = new ViewPort(viewPort.north, viewPort.west, viewPort.south,
          viewPort.east, viewPort.zoom);
      sp.mainSortName = mainSortName;
      sp.queryParams = queryParams;
      sp.makeFuzzy = makeFuzzy;
      sp.fuzzySimilarity = fuzzySimilarity;
      sp.geoSort = geoSort;
      sp.filterChain = filterChain;
      sp.facets = facets;
      sp.geoSortBucketInterval = geoSortBucketInterval;
      sp.geoSortBuckets = geoSortBuckets;
      sp.coordinateQuery = coordinateQuery;
      sp.latField = latField;
      sp.longField = longField;
      return sp;
    }
  }

  @Override
  public SearchParameters createSearchParams(Map<String, String[]> params,
      Reader reader) {
    SuggestSearchParameters searchParams = new SuggestSearchParameters();

    for (String name : params.keySet()) {
      String[] val = params.get(name);
      switch (name) {
      case "offset":
      case "off": {
        searchParams.offset = val == null ? 0 : Integer.parseInt(val[0]);
        break;
      }
      case "limit":
      case "lim": {
        searchParams.limit = val == null ? 0 : Integer.parseInt(val[0]);
        break;
      }
      case "facets": {
        searchParams.enableFacets = val == null ? false : Boolean
            .parseBoolean(val[0]);
        break;
      }
      case "facet": {
        if (val != null && this.faceters != null) {
          searchParams.facets = new HashSet<String>(val.length);
          for (String v : val) {
            if (this.faceters.containsKey(v))
              searchParams.facets.add(v);
          }
        }

        break;
      }
      case "results": {
        searchParams.enableResults = val == null ? true : Boolean
            .parseBoolean(val[0]);
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
      case "lookahead":
      case "lh": {
        searchParams.lookAhead = val == null ? 0 : Integer.parseInt(val[0]);
        break;
      }
      case "qs":
      case "query":
      case "q": {
        searchParams.queryString = val == null ? "" : val[0];
        break;
      }
      case "dedup": {
        searchParams.dedup = val == null ? true : Boolean.parseBoolean(val[0]);
        break;
      }
      case "ms": {
        searchParams.multiSearch = val == null ? true : Boolean
            .parseBoolean(val[0]);
        break;
      }
      case "invertHidden":
      case "invh": {
        searchParams.invertHidden = val == null ? false : Boolean
            .parseBoolean(val[0]);
        break;
      }
      case "flds": {
        if (val != null) {
          searchParams.fieldSelector = fieldSelectors.get(val[0]);
        }
        break;
      }
      case "parser": {
        if (val != null) {
          searchParams.parser = myParsers.get(val[0]);
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

      case "msc":
      case "mainSortChain": {
        if (val != null) {
          searchParams.mainSortName = val[0];
        }
        break;
      }
      case "fc": {
        if (val != null) {
          searchParams.filterChain = this.exprFilters.get(val[0]);
        }
        break;
      }

      case "tt": {
        if (val != null && val[0].equalsIgnoreCase("fuzzy")) {
          searchParams.makeFuzzy = true;
        }
        break;
      }
      case "fs": {
        if (val != null) {
          searchParams.fuzzySimilarity = Float.parseFloat(val[0]);
        }
        break;
      }
      case "geosort": {
        searchParams.geoSort = val == null ? true : Boolean
            .parseBoolean(val[0]);
        break;
      }
      case "gsbi": {
        searchParams.geoSortBucketInterval = val == null ? 0.0d : Double
            .parseDouble(val[0]);
        break;
      }
      case "geobuckets": {
        if (val != null) {
          final double[] buckets;
          DoubleOpenHashSet bucketSet = new DoubleOpenHashSet();
          // HashSet<Integer> bucketSet = new HashSet<>();
          for (String v : val) {
            String[] sbuckets = v.split(",");
            for (String sbucket : sbuckets) {
              bucketSet.add(Double.parseDouble(sbucket.trim()));
            }
          }
          buckets = (double[]) bucketSet.toArray(new double[bucketSet.size()]);
          DoubleArrays.quickSort(buckets, 0, buckets.length,
              DoubleComparators.NATURAL_COMPARATOR);
          searchParams.geoSortBuckets = buckets;
        }
        break;
      }
      case "cq": {
        searchParams.coordinateQuery = val[0];
        break;
      }
      case "latField": {
        searchParams.latField = val[0];
        break;
      }
      case "longField": {
        searchParams.longField = val[0];
        break;
      }
      case "dist": {
        searchParams.dist = val == null ? 10.0f : Double.parseDouble(val[0]);
        // no break here, should also add to query params
      }
      default: {
        if (this.faceters.containsKey(name)) {
          searchParams.facetFilters.put(name,
              faceters.get(name).createFilter(val));
        } else
          searchParams.queryParams.put(name, val);
        break;
      }
      }

    }
    // defaults
    if (searchParams.fieldSelector == null)
      searchParams.fieldSelector = defaultFieldSelector;
    if (searchParams.parser == null)
      searchParams.parser = this.parser;
    if (searchParams.filterChain == null)
      searchParams.filterChain = this.exprFilters.get("default");
    if (searchParams.facets == null && this.faceters != null) {
      searchParams.facets = new HashSet<String>(this.faceters.keySet());
    }
    return searchParams;
  }

  private SearchParameters restrict(SearchParameters params) {
    SuggestSearchParameters nlpsp = (SuggestSearchParameters) params.clone();
    nlpsp.occur = Occur.MUST;
    nlpsp.subOccur = Occur.SHOULD;
    nlpsp.fields = new HashSet<>();
    nlpsp.fields.add("name");
    nlpsp.fields.add("aliases");
    nlpsp.fields.add("geo_path_aliases");
    // nlpsp.fields.add("place_type");
    nlpsp.offset = 0;
    nlpsp.limit = 1;
    nlpsp.fuzzyThreshold = 1;
    return nlpsp;
  }

  private SearchParameters restrict(SearchParameters params,
      boolean excludeFacetFilters) {
    SuggestSearchParameters nlpsp = (SuggestSearchParameters) params.clone();
    nlpsp.occur = Occur.MUST;
    nlpsp.subOccur = Occur.SHOULD;
    nlpsp.fields = new HashSet<>();
    nlpsp.fields.add("name");
    nlpsp.fields.add("aliases");
    nlpsp.fields.add("geo_path_aliases");
    // nlpsp.fields.add("place_type");
    nlpsp.offset = 0;
    nlpsp.limit = 1;
    nlpsp.fuzzyThreshold = 1;
    nlpsp.facetFilters = new HashMap<>();
    return nlpsp;
  }

  @Override
  public Query createQuery(SearchParameters params) throws ParseException,
      IOException {
    return ((SuggestSearchParameters) params).parser.parse(params);
  }

}
