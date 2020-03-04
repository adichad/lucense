package com.adichad.lucense.expression;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.fieldSource.FieldType;

public class FloatLucenseExpression extends LucenseExpression {
  FloatExpressionTree expr;

  public FloatLucenseExpression(ExpressionTree exprTree,
      Map<String, Object2IntOpenHashMap<String>> externalValSources,
      Map<String, LucenseExpression> namedExprs, ValueSources valueSources, Set<String> scoreFields,
      Context cx) throws Exception {
    super(exprTree.getIntVariables(), exprTree.getFloatVariables(), exprTree
        .getDoubleVariables(), exprTree.getBooleanVariables(), exprTree
        .getStringVariables(), externalValSources, namedExprs, valueSources, scoreFields, cx);
    this.expr = (FloatExpressionTree) exprTree;
  }

  @Override
  public LucenseExpression clone() {
    return new FloatLucenseExpression(this);
  }

  public FloatLucenseExpression(FloatLucenseExpression luExpr) {
    super(luExpr);
    this.expr = luExpr.expr.clone();
  }

  public Slates initState(Context cx) {
    return expr.initState(cx);
  }

  @Override
  public ExpressionTree getExpressionTree() {
    return this.expr;
  }

  public float evaluate(int doc, Slates slates, Context cx) throws IOException {
    setVals(doc, slates);
    return this.expr.evaluate(slates, cx);
  }

  public float evaluate(Document doc, Slates slates, Context cx)
      throws NumberFormatException {
    setVals(doc, slates);
    return this.expr.evaluate(slates, cx);
  }

  public float evaluateFinal(Slates slates, Context cx) {
    return this.expr.evaluateFinal(slates, cx);
  }

  @Override
  public FieldType getType() {
    return FieldType.TYPE_FLOAT;
  }

}
