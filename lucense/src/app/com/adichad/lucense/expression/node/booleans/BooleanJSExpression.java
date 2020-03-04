package com.adichad.lucense.expression.node.booleans;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.adichad.lucense.expression.VarContext;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;
import com.adichad.lucense.expression.node.JSExpression;
import com.adichad.lucense.expression.node.doubles.DoubleExpressionNode;
import com.adichad.lucense.expression.node.floats.FloatExpressionNode;
import com.adichad.lucense.expression.node.ints.IntExpressionNode;
import com.adichad.lucense.expression.node.strings.StringExpressionNode;
import com.adichad.lucense.expression.node.strings.StringLiteral;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class BooleanJSExpression extends JSExpression implements BooleanExpressionNode {

  public BooleanJSExpression(StringLiteral exprNode, Context cx, Scriptable scope,
      Map<String, IntExpressionNode> intvars, Map<String, FloatExpressionNode> floatvars,
      Map<String, DoubleExpressionNode> doublevars, Map<String, BooleanExpressionNode> booleanvars,
      Map<String, StringExpressionNode> stringvars) {
    super(exprNode, cx, scope, intvars, floatvars, doublevars, booleanvars, stringvars);
  }

  @Override
  public boolean evaluate(VarContext context, Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    setupScope(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
    return Context.toBoolean(this.script.exec(this.cx, this.scope));
  }

  @Override
  public boolean evaluateFinal(Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    setupScopeFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
    return Context.toBoolean(this.script.exec(this.cx, this.scope));
  }
}
