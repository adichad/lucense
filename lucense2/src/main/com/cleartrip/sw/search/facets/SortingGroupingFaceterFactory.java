package com.cleartrip.sw.search.facets;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.document.Document;
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
import com.cleartrip.sw.search.searchj.CustomFieldSelector;

public class SortingGroupingFaceterFactory extends SearchFaceterFactory {
  private final String field;
  private Term         term;
  private final int    offset;
  private final int    limit;

  public SortingGroupingFaceterFactory(Map<String, ?> params, Properties env) {
    super(params, env);
    this.field = (String) params.get("field");
    this.offset = (Integer) params.get("offset");
    this.limit = (Integer) params.get("limit");
    // System.out.println(offset+", "+limit);
    this.term = new Term(field);

  }

  private static class SortingGroupingFaceter extends SearchFaceter {

    private static class Collectors {
      TopFieldCollector                tfc;
      Collector                        top;
      Map<String, ExpressionCollector> expCols = new HashMap<>();

    }

    // private int docBase;
    private final String                               field;
    private String[]                                   vals;
    private HashMap<String, Collectors>                facets;
    private CustomFieldSelector                        innerFieldSelector;
    private Sort                                       innerSort;
    private int                                        offset;
    private int                                        limit;
    private int                                        collectorLimit;
    private IndexReader                                reader;
    private int                                        docBase;
    private Scorer                                     scorer;
    private Map<String, Object2IntOpenHashMap<String>> evs;
    private Map<String, LucenseExpression>             namedExprs;
    private Set<String>                                scoreFields;
    private ValueSources                               valueSources;
    private Context                                    cx;

    public SortingGroupingFaceter(final String field, Collector next,
        int offset, int limit) {
      super(next);
      this.field = field;
      this.facets = new HashMap<>();

      this.offset = offset;
      this.limit = limit;

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
    public SearchFaceter setOffset(int offset) {
      this.offset = offset;
      this.collectorLimit = offset + limit;
      return this;
    }

    @Override
    public SearchFaceter setLimit(int limit) {
      this.limit = limit;
      this.collectorLimit = offset + limit;
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

    @Override
    public void collect(int doc) throws IOException {
      if (facets.containsKey(vals[doc])) {
        facets.get(vals[doc]).top.collect(doc);
      } else {
        Collectors cols = new Collectors();
        cols.tfc = TopFieldCollector.create(innerSort, collectorLimit, true,
            true, false, true);
        cols.top = cols.tfc;
        try {
          cols.top = innerFieldSelector.getExpressionCollectors(cols.top, evs,
              namedExprs, valueSources, scoreFields, cols.expCols, cx);
        } catch (Exception e) {
          if (e instanceof IOException)
            throw (IOException) e;
          else
            throw new RuntimeException(e);
        }
        cols.top.setNextReader(reader, docBase);
        cols.top.setScorer(scorer);
        facets.put(vals[doc], cols);
        cols.top.collect(doc);
      }
      if (next != null)
        next.collect(doc);
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase)
        throws IOException {
      this.reader = reader;
      this.docBase = docBase;
      this.vals = FieldCache.DEFAULT.getStrings(reader, this.field);
      for (Collectors cols : this.facets.values())
        cols.top.setNextReader(reader, docBase);
      super.setNextReader(reader, docBase);
    }

    @Override
    public void setScorer(Scorer scorer) throws IOException {
      this.scorer = scorer;
      for (Collectors cols : this.facets.values())
        cols.top.setScorer(scorer);
      super.setScorer(scorer);
    }

    @Override
    public String getFacetJson(IndexSearcher searcher, FacetsJsonTopDocs facetsJsonTopDocs)
        throws CorruptIndexException, IOException {

      StringBuilder sb = new StringBuilder();
      sb.append("{ ");
      for (String val : facets.keySet()) {
        sb.append("\"").append(val).append("\": {");

        Collectors cols = facets.get(val);
        TopFieldCollector tfc = cols.tfc;
        TopFieldDocs tfd = (TopFieldDocs) tfc.topDocs(this.offset, this.limit);

        sb.append("\"total_count\":").append(tfd.totalHits).append(",");
        sb.append("\"result_count\":").append(tfd.scoreDocs.length).append(",");
        sb.append("\"results\": [\n");

        for (ScoreDoc sd : tfd.scoreDocs) {
          Document sdoc = searcher.doc(sd.doc, innerFieldSelector);
          sdoc = innerFieldSelector
              .fillAuxFields(searcher, null, sdoc, sd, tfd);
          sdoc = innerFieldSelector
              .fillExpressionValues(cols.expCols, sdoc, sd);
          innerFieldSelector.decantAsJson(sdoc, sb, null, searcher);
          sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1).append("]},");
      }
      sb.deleteCharAt(sb.length() - 1).append("}");
      return sb.toString();
    }
  }

  @Override
  public SearchFaceter createFaceter(Collector c) {
    return new SortingGroupingFaceter(field, c, offset, limit);
  }

  @Override
  public Query createFilter(String[] vals) {
    BooleanQuery q = new BooleanQuery();
    for (String val : vals)
      q.add(new TermQuery(this.term.createTerm(val)), Occur.SHOULD);
    return q;
  }
}
