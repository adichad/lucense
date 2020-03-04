package com.adichad.lucense.expression.node.ints;

import java.util.Map;

import com.adichad.lucense.expression.VarContext;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class IntSignedShiftLeft implements IntExpressionNode {
  private IntExpressionNode shifted;

  private IntExpressionNode shiftby;

  public IntSignedShiftLeft(IntExpressionNode shifted, IntExpressionNode shiftby) {
    this.shifted = shifted;
    this.shiftby = shiftby;
  }

  @Override
  public int evaluate(VarContext context, Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    return this.shifted.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean) << this.shiftby
        .evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
  }

  @Override
  public int evaluateFinal(Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    return this.shifted.evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean) << this.shiftby
        .evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
  }

}
