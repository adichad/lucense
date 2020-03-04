package com.adichad.lucense.expression;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.Map;
import java.util.Set;

import org.mozilla.javascript.Context;

public class ExpressionComparatorSourceFactory {
  private ExpressionTree expressionTree;

  public ExpressionComparatorSourceFactory(ExpressionTree tree) {
    this.expressionTree = tree;
  }

  public ExpressionComparatorSource newExpressionComparatorSource(
      Map<String, Object2IntOpenHashMap<String>> externalValSource,
      Map<String, LucenseExpression> namedExprs, ValueSources valueSources, Set<String> scoreFields, 
      Context cx) throws Exception {
    return new ExpressionComparatorSource(
        ExpressionFactory.getExpressionFromTree(expressionTree,
            externalValSource, namedExprs, valueSources, scoreFields, cx));
  }
}
