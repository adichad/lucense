package com.cleartrip.sw.search.facets;

import it.unimi.dsi.fastutil.doubles.Double2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectSortedMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.TopFieldDocs;
import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.ExpressionCollector;
import com.adichad.lucense.expression.LucenseExpression;
import com.adichad.lucense.expression.ValueSources;
import com.cleartrip.sw.search.map.ViewPort;
import com.cleartrip.sw.search.searchj.CustomFieldSelector;

public class GridFaceterFactory extends SearchFaceterFactory {
  private Term                term;
  private final String        latField;
  private final String        lngField;
  private ViewPort            fullView;
  private List<Double>        latSteps;
  private List<Double>        lngSteps;
  private final List<Integer> zoomLimits;

  public GridFaceterFactory(Map<String, ?> params, Properties env) {
    super(params, env);
    this.latField = (String) params.get("latField");
    this.lngField = (String) params.get("lngField");

    double latMin = (Double) params.get("latMin");
    double latMax = (Double) params.get("latMax");
    double lngMin = (Double) params.get("lngMin");
    double lngMax = (Double) params.get("lngMax");
    this.fullView = new ViewPort(latMax, lngMin, latMin, lngMax, 0);
    List<Double> latRatios = (List<Double>) params.get("latRatios");
    List<Double> lngRatios = (List<Double>) params.get("lngRatios");
    this.zoomLimits = (List<Integer>) params.get("zoomLimit");
    // System.out.println(latRatios);

    // System.out.println(lngRatios);
    this.latSteps = new ArrayList<>(latRatios.size());
    this.lngSteps = new ArrayList<>(lngRatios.size());

    double latRange = (fullView.north - fullView.south);
    double ratio = 1;
    for (int i = 0; i < latRatios.size(); i++) {
      ratio *= latRatios.get(i);
      this.latSteps.add(latRange * ratio);
    }

    double lngRange = (fullView.east - fullView.west);
    ratio = 1;
    for (int i = 0; i < lngRatios.size(); i++) {
      ratio *= lngRatios.get(i);
      this.lngSteps.add(lngRange * ratio);
    }
    // System.out.println(latSteps);
    // System.out.println(lngSteps);

    // System.out.println(offset+", "+limit);

  }

  private static class GridFaceter extends SearchFaceter {

    private static class Collectors {
      TopFieldCollector                tfc;
      Collector                        top;
      Map<String, ExpressionCollector> expCols = new HashMap<>();

    }

    // private int docBase;
    // private HashMap<String, Collectors> facets;
    private CustomFieldSelector                                                innerFieldSelector;
    private Sort                                                               innerSort;
    private int                                                                collectorLimit;
    private Map<String, Object2IntOpenHashMap<String>>                         evs;
    private Map<String, LucenseExpression>                                     namedExprs;
    private Set<String>                                                        scoreFields;
    private ValueSources                                                       valueSources;
    private Context                                                            cx;
    private final Double2ObjectAVLTreeMap<Double2ObjectAVLTreeMap<Collectors>> viewPortFinder;
    private double[]                                                           lats;
    private double[]                                                           lngs;
    private final String                                                       latField;
    private final String                                                       lngField;
    private final List<Integer>                                                zoomLimits;
    private final List<Double>                                                 latSteps;
    private final List<Double>                                                 lngSteps;
    private final ViewPort                                                     fullView;

    public GridFaceter(final Collector next, List<Integer> zoomLimits,
        List<Double> latSteps, List<Double> lngSteps, ViewPort fullViewPort,
        final String latField, final String lngField) {
      super(next);

      this.zoomLimits = zoomLimits;
      this.latSteps = latSteps;
      this.lngSteps = lngSteps;
      this.fullView = fullViewPort;
      this.latField = latField;
      this.lngField = lngField;
      this.viewPortFinder = new Double2ObjectAVLTreeMap<>();

    }

    @Override
    public SearchFaceter setSort(Sort sort) {
      this.innerSort = sort;
      return this;
    }

    @Override
    public SearchFaceter setFieldSelector(CustomFieldSelector fieldSelector) {
      this.innerFieldSelector = fieldSelector;
      return this;
    }

    @Override
    public SearchFaceter setExpressionStuff(
        Map<String, Object2IntOpenHashMap<String>> externalValSource,
        Map<String, LucenseExpression> namedExprs, ValueSources valueSources,
        Set<String> scoreFields, Context cx) {
      this.evs = externalValSource;
      this.namedExprs = namedExprs;
      this.valueSources = valueSources;
      this.scoreFields = scoreFields;
      this.cx = cx;
      return this;
    }

    private ViewPort snapToGrid(ViewPort viewPort) {
      double latStep = this.latSteps.get(viewPort.zoom);
      double lngStep = this.lngSteps.get(viewPort.zoom);
      viewPort.snapToGrid(fullView, latStep, lngStep);
      return viewPort;
    }

    @Override
    public SearchFaceter setViewPort(ViewPort vp) throws IOException {
      vp = snapToGrid(vp);

      // System.out.println("snapped viewport: "+vp);
      double nsStep = latSteps.get(vp.zoom);
      double ewStep = lngSteps.get(vp.zoom);
      // System.out.println("ns-step:"+nsStep);
      // System.out.println("ew-step:"+ewStep);
      // int gridCount = 0;
      this.collectorLimit = zoomLimits.get(vp.zoom);
      for (double currSouth = vp.south; currSouth < vp.north; currSouth += nsStep) {
        for (double currWest = vp.west; currWest < vp.east; currWest += ewStep) {
          // System.out.println("added collector: (" + currSouth + ", " +
          // currWest
          // + ")");

          Double2ObjectAVLTreeMap<Collectors> m;
          if (!viewPortFinder.containsKey(currSouth)) {
            m = new Double2ObjectAVLTreeMap<>();
            viewPortFinder.put(currSouth, m);
          } else {
            m = viewPortFinder.get(currSouth);
          }
          if (!m.containsKey(currWest)) {
            Collectors cols = new Collectors();
            cols.tfc = TopFieldCollector.create(innerSort, collectorLimit,
                true, true, false, true);
            cols.top = cols.tfc;
            try {
              cols.top = innerFieldSelector.getExpressionCollectors(cols.top,
                  evs, namedExprs, valueSources, scoreFields, cols.expCols, cx);
            } catch (Exception e) {
              if (e instanceof IOException)
                throw (IOException) e;
              else
                throw new RuntimeException(e);
            }
            // gridCount++;
            m.put(currWest, cols);
          }
        }
      }
      // System.out.println("added "+gridCount+" collectors");
      return this;
    }

    @Override
    public void collect(int doc) throws IOException {
      Double2ObjectSortedMap<Double2ObjectAVLTreeMap<Collectors>> om = viewPortFinder
          .headMap(lats[doc]);
      if (om.isEmpty())
        return;
      double gridLat = om.lastDoubleKey();
      Double2ObjectSortedMap<Collectors> im = viewPortFinder.get(gridLat)
          .headMap(lngs[doc]);
      if (im.isEmpty())
        return;
      double gridLong = im.lastDoubleKey();
      Collectors cols = viewPortFinder.get(gridLat).get(gridLong);
      cols.top.collect(doc);

      if (next != null)
        next.collect(doc);
      // System.out.println("collected doc into: (" + gridLat + ", " + gridLong
      // + ")");
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase)
        throws IOException {
      this.lats = FieldCache.DEFAULT.getDoubles(reader, this.latField);
      this.lngs = FieldCache.DEFAULT.getDoubles(reader, this.lngField);
      for (Map<Double, Collectors> m : this.viewPortFinder.values()) {
        for (Collectors cols : m.values())
          cols.top.setNextReader(reader, docBase);
      }

      super.setNextReader(reader, docBase);
    }

    @Override
    public void setScorer(Scorer scorer) throws IOException {
      for (Map<Double, Collectors> m : this.viewPortFinder.values()) {
        for (Collectors cols : m.values())
          cols.top.setScorer(scorer);
      }
      super.setScorer(scorer);
    }

    @Override
    public String getFacetJson(IndexSearcher searcher, FacetsJsonTopDocs facetsJsonTopDocs)
        throws CorruptIndexException, IOException {

      int totalCount = 0;

      StringBuilder sb = new StringBuilder();
      sb.append("\"results\": [\n");
      NumericField gridid = new NumericField("grid_id", 4, Store.YES, false);
      int f = 0;
      int i = 0;
      for (Map<Double, Collectors> m : this.viewPortFinder.values()) {
        for (Collectors cols : m.values()) {
          TopFieldCollector tfc = cols.tfc;
          TopFieldDocs tfd = (TopFieldDocs) tfc.topDocs(0, this.collectorLimit);
          totalCount += tfd.totalHits;
          ScoreDoc[] sds = tfd.scoreDocs;
          for (ScoreDoc sd : sds) {
            Document sdoc = searcher.doc(sd.doc, innerFieldSelector);
            sdoc = innerFieldSelector.fillAuxFields(searcher, null, sdoc, sd,
                tfd);
            sdoc = innerFieldSelector.fillExpressionValues(cols.expCols, sdoc,
                sd);
            sdoc.add(gridid.setIntValue(i));
            innerFieldSelector.decantAsJson(sdoc, sb, null, searcher);
            sb.append(",");
            f++;
          }
          i++;
          // System.out.println(tfd.totalHits+"=>"+sds.length);
        }
      }
      sb.insert(0, ",").insert(0, totalCount).insert(0, "\"total_count\":");
      sb.insert(0, "{ ");

      sb.deleteCharAt(sb.length() - 1).append("],");
      sb.append("\"result_count\":").append(f).append(",");

      sb.deleteCharAt(sb.length() - 1).append("}");
      // System.out.println("generated facet json");
      return sb.toString();

      /*
       * ArrayList<ScoreDoc[]> sdarrs = new ArrayList<>(limit);
       * ArrayList<Collectors> colsarr = new ArrayList<>(limit);
       * 
       * int myLim = totalCount > collectorLimit ? collectorLimit : totalCount;
       * int sizeLastCycle = 0; int i = 0; int j = 0; int k = 0; int f = 0;
       * 
       * StringBuilder sb = new StringBuilder(); sb.append("{ ");
       * sb.append("\"total_count\":").append(totalCount).append(",");
       * sb.append("\"results\": [\n");
       * 
       * // System.out.println("found " + sdarrs.size() + " non-empty grids");
       * if (totalCount > 0) do { sizeLastCycle = k; while (sizeLastCycle == k)
       * { ScoreDoc[] currsds = sdarrs.get(i); Collectors cols = colsarr.get(i);
       * if (j < currsds.length) { if (k >= offset) { Document sdoc = searcher
       * .doc(currsds[j].doc, innerFieldSelector); sdoc =
       * innerFieldSelector.fillAuxFields(searcher, null, sdoc, currsds[j]);
       * sdoc = innerFieldSelector.fillExpressionValues(cols.expCols, sdoc,
       * currsds[j]); innerFieldSelector.decantAsJson(sdoc, sb, null, searcher);
       * sb.append(","); ++f; // System.out.println("found: "+f); } ++k; //
       * System.out.println("found from start: "+k); } //
       * System.out.println("from collector: "+i); i = (i + 1) % sdarrs.size();
       * if (i == 0) j++;
       * 
       * }
       * 
       * // System.out.println("f: "+f+", limit: "+limit+", sizeLastCycle: "+
       * sizeLastCycle+", k: "+k+", j: "+j+", i: "+i); } while (k < myLim &&
       * sizeLastCycle < k); sb.deleteCharAt(sb.length() - 1).append("],");
       * sb.append("\"result_count\":").append(f).append(",");
       * 
       * sb.deleteCharAt(sb.length() - 1).append("}"); //
       * System.out.println("generated facet json"); return sb.toString();
       */
    }
  }

  @Override
  public SearchFaceter createFaceter(Collector c) {
    return new GridFaceter(c, zoomLimits, latSteps, lngSteps, fullView,
        latField, lngField);
  }

  @Override
  public Query createFilter(String[] vals) {
    BooleanQuery q = new BooleanQuery();
    for (String val : vals)
      q.add(new TermQuery(this.term.createTerm(val)), Occur.SHOULD);
    return q;
  }
}
