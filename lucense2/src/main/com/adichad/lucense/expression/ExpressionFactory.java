package com.adichad.lucense.expression;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.StringReader;
import java.util.Map;
import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.adichad.lucense.expression.parse.ExpressionParser;

public class ExpressionFactory {

  public static LucenseExpression getExpressionFromString(String expr,
      Scriptable scope,
      Map<String, Object2IntOpenHashMap<String>> externalValSource,
      Map<String, LucenseExpression> namedExprs, ValueSources valueSources, Set<String> scoreFields,
      Context cx) {
    // System.out.println("parsing: "+expr);
    try {
      if ((expr != null) && !expr.trim().equals("")) {
        ExpressionParser parser = new ExpressionParser(new StringReader(expr));
        ExpressionTree tree = parser.parse(scope);

        return getExpressionFromTree(tree, externalValSource,
            namedExprs, valueSources, scoreFields, cx);
      }
    } catch (Throwable e) {
      throw new RuntimeException(expr, e);
    }
    return null;
  }

  public static ExpressionTree getExpressionTreeFromString(String expr,
      Scriptable scope) {
    // System.out.println("parsing: "+expr);
    try {
      if ((expr != null) && !expr.trim().equals("")) {
        ExpressionParser parser = new ExpressionParser(new StringReader(expr));
        ExpressionTree tree = parser.parse(scope);
        return tree;
      }
    } catch (Throwable e) {
      throw new RuntimeException(expr, e);
    }
    return null;
  }

  public static LucenseExpression getExpressionFromTree(ExpressionTree tree, Map<String, Object2IntOpenHashMap<String>> externalValSource,
      Map<String, LucenseExpression> namedExprs, ValueSources valueSources, Set<String> scoreFields,
      Context cx) throws Exception {
    
    switch (tree.getType()) {
    case TYPE_DOUBLE:
      return new DoubleLucenseExpression(tree, externalValSource,
          namedExprs, valueSources, scoreFields, cx);
    case TYPE_STRING:
      return new StringLucenseExpression(tree, externalValSource,
          namedExprs, valueSources, scoreFields, cx);
    case TYPE_FLOAT:
      return new FloatLucenseExpression(tree, externalValSource,
          namedExprs, valueSources, scoreFields, cx);
    case TYPE_INT:
      return new IntLucenseExpression(tree, externalValSource, namedExprs,
          valueSources, scoreFields, cx);
    case TYPE_BOOLEAN:
      return new BooleanLucenseExpression(tree, externalValSource,
          namedExprs, valueSources, scoreFields, cx);
    default:
      return new IntLucenseExpression(tree, externalValSource, namedExprs,
          valueSources, scoreFields, cx);
    }
  }
}
