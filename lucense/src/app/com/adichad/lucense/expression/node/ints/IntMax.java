package com.adichad.lucense.expression.node.ints;

import java.util.List;
import java.util.Map;

import com.adichad.lucense.expression.VarContext;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class IntMax implements IntExpressionNode {
  List<IntExpressionNode> children;

  public IntMax(List<IntExpressionNode> children) {
    this.children = children;
  }

  @Override
  public int evaluate(VarContext context, Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    int max = this.children.get(0).evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
    for (int i = 1; i < this.children.size(); i++) {
      int curr = this.children.get(i).evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
      if (curr > max)
        max = curr;
    }
    return max;
  }

  @Override
  public int evaluateFinal(Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    int max = this.children.get(0).evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
    for (int i = 1; i < this.children.size(); i++) {
      int curr = this.children.get(i).evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
      if (curr > max)
        max = curr;
    }
    return max;
  }
}
