package com.adichad.lucense.expression.node.booleans;

import java.util.Map;

import com.adichad.lucense.expression.VarContext;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;
import com.adichad.lucense.expression.node.strings.StringExpressionNode;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class BooleanNotEqualString implements BooleanExpressionNode {
  StringExpressionNode left;

  StringExpressionNode right;

  public BooleanNotEqualString(StringExpressionNode left, StringExpressionNode right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public boolean evaluate(VarContext context, Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    return !this.left.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean).equals(
        (this.right.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean)));
  }

  @Override
  public boolean evaluateFinal(Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    return !this.left.evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean).equals(
        (this.right.evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean)));
  }
}
