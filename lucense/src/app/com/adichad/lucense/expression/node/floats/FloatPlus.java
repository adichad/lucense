package com.adichad.lucense.expression.node.floats;

import java.util.List;
import java.util.Map;

import com.adichad.lucense.expression.VarContext;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class FloatPlus implements FloatExpressionNode {
  List<FloatExpressionNode> children;

  public FloatPlus(List<FloatExpressionNode> children) {
    this.children = children;
  }

  @Override
  public float evaluate(VarContext context, Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    float i = 0f;
    for (FloatExpressionNode child : this.children) {
      i += child.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
    }
    return i;
  }

  @Override
  public float evaluateFinal(Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    float i = 0f;
    for (FloatExpressionNode child : this.children) {
      i += child.evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
    }
    return i;
  }

}
