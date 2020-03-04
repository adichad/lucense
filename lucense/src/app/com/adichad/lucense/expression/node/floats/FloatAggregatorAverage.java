package com.adichad.lucense.expression.node.floats;

import java.util.Map;

import com.adichad.lucense.expression.VarContext;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class FloatAggregatorAverage implements AggregatorExpressionNode, FloatExpressionNode {
  FloatExpressionNode child;

  public FloatAggregatorAverage(FloatExpressionNode child) {
    this.child = child;
  }

  @Override
  public float evaluate(VarContext context, Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    FloatAggregatorAverageSlate state = (FloatAggregatorAverageSlate) slateObject.get(this);
    state.i += this.child.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
    state.n++;
    return 0;
  }

  @Override
  public float evaluateFinal(Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    FloatAggregatorAverageSlate state = (FloatAggregatorAverageSlate) slateObject.get(this);
    if (state.n == 0)
      return Float.NaN;
    return state.i / ((Integer) state.n).floatValue();
  }

  @Override
  public void reset() {

  }

  public class FloatAggregatorAverageSlate {
    float i = 0f;

    int n = 0;
  }

  @Override
  public void initSlate(Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    slateObject.put(this, new FloatAggregatorAverageSlate());
  }

  @Override
  public void updateState(VarContext context, Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    FloatAggregatorAverageSlate state = (FloatAggregatorAverageSlate) slateObject.get(this);
    state.i += this.child.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
    state.n++;
  }

}
