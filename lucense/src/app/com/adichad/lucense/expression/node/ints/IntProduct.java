package com.adichad.lucense.expression.node.ints;

import java.util.List;
import java.util.Map;

import com.adichad.lucense.expression.VarContext;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class IntProduct implements IntExpressionNode {
  List<IntExpressionNode> children;

  public IntProduct(List<IntExpressionNode> children) {
    this.children = children;
  }

  @Override
  public int evaluate(VarContext context, Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    int i = 1;
    for (IntExpressionNode child : this.children) {
      i *= child.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
      if (i == 0)
        return 0;
    }
    return i;
  }

  @Override
  public int evaluateFinal(Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    int i = 1;
    for (IntExpressionNode child : this.children) {
      i *= child.evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
      if (i == 0)
        return 0;
    }
    return i;
  }
}
