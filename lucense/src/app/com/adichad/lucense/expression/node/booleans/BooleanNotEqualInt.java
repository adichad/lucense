package com.adichad.lucense.expression.node.booleans;

import java.util.Map;

import com.adichad.lucense.expression.VarContext;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;
import com.adichad.lucense.expression.node.ints.IntExpressionNode;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class BooleanNotEqualInt implements BooleanExpressionNode {
  IntExpressionNode left;

  IntExpressionNode right;

  public BooleanNotEqualInt(IntExpressionNode left, IntExpressionNode right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public boolean evaluate(VarContext context, Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    return this.left.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean) != (this.right
        .evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean));
  }

  @Override
  public boolean evaluateFinal(Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    return this.left.evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean) != (this.right
        .evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean));
  }
}
