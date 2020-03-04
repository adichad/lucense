package com.adichad.lucense.expression;

import java.io.StringReader;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.adichad.lucense.expression.parse.ExpressionParser;
import com.adichad.lucense.request.Request.FieldType;
import com.adichad.lucense.resource.SearchResourceManager;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class ExpressionFactory {

  public static LucenseExpression getExpressionFromString(String expr, FieldType exprType, Context cx,
      Scriptable scope, Map<String, Object2IntOpenHashMap<String>> externalValSource,
      Map<String, LucenseExpression> namedExprs, ValueSources valueSources, SearchResourceManager srm) {
    // System.out.println("parsing: "+expr);
    try {
      if ((expr != null) && !expr.trim().equals("") && (cx != null)) {
        ExpressionParser parser = new ExpressionParser(new StringReader(expr));
        ExpressionTree tree = parser.parse(cx, scope);

        switch (exprType) {
        case TYPE_DOUBLE:
          return new DoubleLucenseExpression(tree, externalValSource, namedExprs, valueSources.intValueSources,
              valueSources.floatValueSources, valueSources.doubleValueSources, valueSources.booleanValueSources,
              valueSources.stringValueSources, srm);
        case TYPE_STRING:
          return new StringLucenseExpression(tree, externalValSource, namedExprs, valueSources.intValueSources,
              valueSources.floatValueSources, valueSources.doubleValueSources, valueSources.booleanValueSources,
              valueSources.stringValueSources, srm);
        case TYPE_FLOAT:
          return new FloatLucenseExpression(tree, externalValSource, namedExprs, valueSources.intValueSources,
              valueSources.floatValueSources, valueSources.doubleValueSources, valueSources.booleanValueSources,
              valueSources.stringValueSources, srm);
        case TYPE_INT:
          return new IntLucenseExpression(tree, externalValSource, namedExprs, valueSources.intValueSources,
              valueSources.floatValueSources, valueSources.doubleValueSources, valueSources.booleanValueSources,
              valueSources.stringValueSources, srm);
        case TYPE_BOOLEAN:
          return new BooleanLucenseExpression(tree, externalValSource, namedExprs, valueSources.intValueSources,
              valueSources.floatValueSources, valueSources.doubleValueSources, valueSources.booleanValueSources,
              valueSources.stringValueSources, srm);
        default:
          return new IntLucenseExpression(tree, externalValSource, namedExprs, valueSources.intValueSources,
              valueSources.floatValueSources, valueSources.doubleValueSources, valueSources.booleanValueSources,
              valueSources.stringValueSources, srm);
        }
      }
    } catch (Throwable e) {
      throw new RuntimeException(expr, e);
    }
    return null;
  }

  public static ExpressionTree getExpressionTreeFromString(String expr, Context cx, Scriptable scope) {
    // System.out.println("parsing: "+expr);
    try {
      if ((expr != null) && !expr.trim().equals("") && (cx != null)) {
        ExpressionParser parser = new ExpressionParser(new StringReader(expr));
        ExpressionTree tree = parser.parse(cx, scope);
        return tree;
      }
    } catch (Throwable e) {
      throw new RuntimeException(expr, e);
    }
    return null;
  }

}
