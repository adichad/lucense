package com.adichad.lucense.expression.node.floats;

import java.util.List;
import java.util.Map;

import com.adichad.lucense.expression.VarContext;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class FloatMinus implements FloatExpressionNode {
  List<FloatExpressionNode> negChildren;

  FloatExpressionNode posChild;

  public FloatMinus(FloatExpressionNode posChild, List<FloatExpressionNode> negChildren) {
    this.posChild = posChild;
    this.negChildren = negChildren;
  }

  @Override
  public float evaluate(VarContext context, Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    float i = this.posChild.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
    for (FloatExpressionNode child : this.negChildren) {
      i -= child.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
    }
    return i;
  }

  @Override
  public float evaluateFinal(Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    float i = this.posChild.evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
    for (FloatExpressionNode child : this.negChildren) {
      i -= child.evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
    }
    return i;
  }

}
