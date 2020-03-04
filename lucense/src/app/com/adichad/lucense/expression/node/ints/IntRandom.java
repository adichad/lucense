package com.adichad.lucense.expression.node.ints;

import java.util.Map;

import com.adichad.lucense.expression.VarContext;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class IntRandom implements IntExpressionNode {
  IntExpressionNode from, to;

  public IntRandom(IntExpressionNode from, IntExpressionNode to) {
    this.from = from;
    this.to = to;
  }

  @Override
  public int evaluate(VarContext context, Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    int f = this.from.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
    return ((Double) (Math.random()
        * (this.to.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean) - f) + f))
        .intValue();
  }

  @Override
  public int evaluateFinal(Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    int f = this.from.evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
    return ((Double) (Math.random()
        * (this.to.evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean) - f) + f)).intValue();
  }

}
