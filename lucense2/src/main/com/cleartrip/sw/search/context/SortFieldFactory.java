package com.cleartrip.sw.search.context;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.Map;
import java.util.Set;

import org.apache.lucene.search.SortField;
import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.ExpressionComparatorSourceFactory;
import com.adichad.lucense.expression.LucenseExpression;
import com.adichad.lucense.expression.ValueSources;

public class SortFieldFactory {
  private static final Context              cx = null;
  private SortField                         sf;
  private ExpressionComparatorSourceFactory ecsf;
  private String                            field;
  private boolean                           descending;

  public SortFieldFactory(SortField sf) {
    this.sf = sf;
  }

  public SortFieldFactory(ExpressionComparatorSourceFactory ecsf,
      boolean descending, String field) {
    this.ecsf = ecsf;
    this.field = field;
    this.descending = descending;
  }

  public SortField getSortField(
      Map<String, Object2IntOpenHashMap<String>> externalValSource,
      Map<String, LucenseExpression> namedExprs, ValueSources valueSources, Set<String> scoreFields)
      throws Exception {
    if (sf != null)
      return sf;
    return new SortField(field, ecsf.newExpressionComparatorSource(
        externalValSource, namedExprs, valueSources, scoreFields, cx), descending);
  }
}
