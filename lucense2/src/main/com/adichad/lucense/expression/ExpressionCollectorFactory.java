package com.adichad.lucense.expression;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.Map;
import java.util.Set;

import org.apache.lucene.search.Collector;
import org.mozilla.javascript.Context;

public class ExpressionCollectorFactory {
  private ExpressionTree tree;

  public ExpressionCollectorFactory(ExpressionTree tree) {
    this.tree = tree;
  }

  public ExpressionCollector getCollector(Collector c,
      Map<String, Object2IntOpenHashMap<String>> externalValSource,
      Map<String, LucenseExpression> namedExprs, ValueSources valueSources,
      Set<String> scoreFields, Context cx) throws Exception {
    LucenseExpression expr = ExpressionFactory.getExpressionFromTree(tree,
        externalValSource, namedExprs, valueSources, scoreFields, cx);
    switch (expr.getType()) {
    case TYPE_INT:
      return new IntExpressionCollector((IntLucenseExpression) expr, c, cx);
    case TYPE_BOOLEAN:
      return new BooleanExpressionCollector((BooleanLucenseExpression) expr, c,
          cx);
    case TYPE_DOUBLE:
      return new DoubleExpressionCollector((DoubleLucenseExpression) expr, c,
          cx);
    case TYPE_FLOAT:
      return new FloatExpressionCollector((FloatLucenseExpression) expr, c, cx);
    case TYPE_STRING:
      return new StringExpressionCollector((StringLucenseExpression) expr, c,
          cx);
    default:
      return new IntExpressionCollector((IntLucenseExpression) expr, c, cx);
    }
  }
}
