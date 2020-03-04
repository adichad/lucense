package com.adichad.lucense.expression.node.strings;

import java.util.List;
import java.util.Map;

import com.adichad.lucense.expression.VarContext;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class StringConcat implements StringExpressionNode {
  List<StringExpressionNode> children;

  public StringConcat(List<StringExpressionNode> children) {
    this.children = children;
  }

  @Override
  public String evaluate(VarContext context, Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    StringBuilder sb = new StringBuilder();
    for (StringExpressionNode child : this.children) {
      sb.append(child.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean));
    }
    return sb.toString();
  }

  @Override
  public String evaluateFinal(Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    StringBuilder sb = new StringBuilder();
    for (StringExpressionNode child : this.children) {
      sb.append(child.evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean));
    }
    return sb.toString();
  }

}
