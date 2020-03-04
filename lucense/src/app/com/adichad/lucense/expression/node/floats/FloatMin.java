package com.adichad.lucense.expression.node.floats;

import java.util.List;
import java.util.Map;

import com.adichad.lucense.expression.VarContext;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class FloatMin implements FloatExpressionNode {
  List<FloatExpressionNode> children;

  public FloatMin(List<FloatExpressionNode> children) {
    this.children = children;
  }

  @Override
  public float evaluate(VarContext context, Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    float min = this.children.get(0).evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
    for (int i = 1; i < this.children.size(); i++) {
      float curr = this.children.get(i).evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
      if (curr < min)
        min = curr;
    }
    return min;
  }

  @Override
  public float evaluateFinal(Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    float min = this.children.get(0).evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
    for (int i = 1; i < this.children.size(); i++) {
      float curr = this.children.get(i).evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
      if (curr < min)
        min = curr;
    }
    return min;
  }
}
