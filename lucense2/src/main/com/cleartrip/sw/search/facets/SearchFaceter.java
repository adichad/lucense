package com.cleartrip.sw.search.facets;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopFieldCollector;
import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.LucenseExpression;
import com.adichad.lucense.expression.ValueSources;
import com.cleartrip.sw.search.map.ViewPort;
import com.cleartrip.sw.search.searchj.CustomFieldSelector;

public abstract class SearchFaceter extends Collector {
  protected Collector next;

  public SearchFaceter(Collector next) {
    this.next = next;
  }

  @Override
  public void setNextReader(IndexReader reader, int docBase) throws IOException {
    if (next != null)
      next.setNextReader(reader, docBase);
  }

  @Override
  public void setScorer(Scorer scorer) throws IOException {
    if (next != null)
      next.setScorer(scorer);
  }

  @Override
  public final boolean acceptsDocsOutOfOrder() {
    if (next != null)
      return next.acceptsDocsOutOfOrder();
    return true;
  }

  public SearchFaceter setSort(Sort sort) {
    return this;
  }

  public SearchFaceter setFieldSelector(CustomFieldSelector fieldSelector) {
    return this;
  }
  
  public SearchFaceter setTFC(TopFieldCollector tfc, IndexReader reader,int docId)
		  throws CorruptIndexException, IOException {
    return this;
  }

  public SearchFaceter setOffset(int offset) {
    return this;
  }

  public SearchFaceter setLimit(int limit) {
    return this;
  }

  public SearchFaceter setViewPort(ViewPort vp) throws IOException {
    return this;
  }

  public SearchFaceter setExpressionStuff(
      Map<String, Object2IntOpenHashMap<String>> externalValSource,
      Map<String, LucenseExpression> namedExprs, ValueSources valueSources,
      Set<String> scoreFields, Context cx) {
    return this;
  }

  public abstract String getFacetJson(IndexSearcher searcher, FacetsJsonTopDocs facetsJsonTopDocs)
      throws IOException;

}
