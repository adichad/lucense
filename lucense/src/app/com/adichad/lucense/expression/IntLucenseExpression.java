package com.adichad.lucense.expression;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.document.Document;

import com.adichad.lucense.expression.fieldSource.BooleanValueSource;
import com.adichad.lucense.expression.fieldSource.DoubleValueSource;
import com.adichad.lucense.expression.fieldSource.FloatValueSource;
import com.adichad.lucense.expression.fieldSource.IntValueSource;
import com.adichad.lucense.expression.fieldSource.StringValueSource;
import com.adichad.lucense.request.Request.FieldType;
import com.adichad.lucense.resource.SearchResourceManager;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class IntLucenseExpression extends LucenseExpression {
  IntExpressionTree expr;

  public IntLucenseExpression(ExpressionTree exprTree, Map<String, Object2IntOpenHashMap<String>> externalValSources,
      Map<String, LucenseExpression> namedExprs, Map<String, IntValueSource> intValueSources,
      Map<String, FloatValueSource> floatValueSources, Map<String, DoubleValueSource> doubleValueSources,
      Map<String, BooleanValueSource> booleanValueSources, Map<String, StringValueSource> stringValueSources,
      SearchResourceManager srm) throws Exception {
    super(exprTree.getIntVariables(), exprTree.getFloatVariables(), exprTree.getDoubleVariables(), exprTree
        .getBooleanVariables(), exprTree.getStringVariables(), externalValSources, namedExprs, intValueSources,
        floatValueSources, doubleValueSources, stringValueSources, booleanValueSources, srm);
    this.expr = (IntExpressionTree) exprTree;

  }

  @Override
  public LucenseExpression clone() {
    return new IntLucenseExpression(this);

  }

  public IntLucenseExpression(IntLucenseExpression luExpr) {
    super(luExpr);
    this.expr = luExpr.expr.clone();

  }

  @Override
  public ExpressionTree getExpressionTree() {
    return this.expr;
  }

  public int evaluate(int doc) throws IOException {
    setVals(doc, this.expr);
    return this.expr.evaluate();
  }

  public int evaluate(Document doc) throws NumberFormatException {
    setVals(doc, this.expr);
    return this.expr.evaluate();
  }

  public int evaluateFinal() {
    return this.expr.evaluateFinal();
  }

  @Override
  public FieldType getType() {
    return FieldType.TYPE_INT;
  }

}
