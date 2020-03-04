package com.adichad.lucense.expression.node.floats;

import java.util.Map;

import com.adichad.lucense.expression.VarContext;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class FloatDivide implements FloatExpressionNode {
  private FloatExpressionNode dividend;

  private FloatExpressionNode divisor;

  public FloatDivide(FloatExpressionNode dividend, FloatExpressionNode divisor) {
    this.dividend = dividend;
    this.divisor = divisor;
  }

  @Override
  public float evaluate(VarContext context, Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    return this.dividend.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean)
        / this.divisor.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
  }

  @Override
  public float evaluateFinal(Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    return this.dividend.evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean)
        / this.divisor.evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
  }

}
