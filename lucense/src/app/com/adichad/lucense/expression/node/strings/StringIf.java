package com.adichad.lucense.expression.node.strings;

import java.util.Map;

import com.adichad.lucense.expression.VarContext;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;
import com.adichad.lucense.expression.node.booleans.BooleanExpressionNode;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class StringIf implements StringExpressionNode {
  BooleanExpressionNode condition;

  StringExpressionNode nthen;

  StringExpressionNode nelse;

  public StringIf(BooleanExpressionNode condition, StringExpressionNode nthen, StringExpressionNode nelse) {
    this.condition = condition;
    this.nthen = nthen;
    this.nelse = nelse;
  }

  @Override
  public String evaluate(VarContext context, Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    if (this.condition.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean))
      return this.nthen.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
    return this.nelse.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean);

  }

  @Override
  public String evaluateFinal(Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    if (this.condition.evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean))
      return this.nthen.evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
    return this.nelse.evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
  }

}
