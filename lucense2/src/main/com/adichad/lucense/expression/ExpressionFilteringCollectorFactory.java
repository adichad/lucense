package com.adichad.lucense.expression;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.Map;
import java.util.Set;

import org.apache.lucene.search.Collector;
import org.mozilla.javascript.Context;

public class ExpressionFilteringCollectorFactory {
  private final BooleanExpressionTree tree;
  private final boolean               exclude;

  public ExpressionFilteringCollectorFactory(BooleanExpressionTree tree,
      boolean exclude) {
    this.tree = tree;
    this.exclude = exclude;
  }

  public ExpressionFilteringCollector getCollector(Collector c,
      Map<String, Object2IntOpenHashMap<String>> externalValSource,
      Map<String, LucenseExpression> namedExprs, ValueSources valueSources, Set<String> scoreFields,
      Context cx) throws Exception {
    BooleanLucenseExpression expr = (BooleanLucenseExpression) ExpressionFactory
        .getExpressionFromTree(tree, externalValSource, namedExprs,
            valueSources, scoreFields, cx);
    return new ExpressionFilteringCollector(c, expr, exclude, cx);
  }
}
