package com.adichad.lucense.expression.node.strings;

import java.util.Map;

import com.adichad.lucense.expression.VarContext;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class StringAggregatorConcat implements AggregatorExpressionNode, StringExpressionNode {
  StringExpressionNode child;

  // StringBuilder sb = new StringBuilder();

  public StringAggregatorConcat(StringExpressionNode child) {
    this.child = child;
  }

  @Override
  public String evaluate(VarContext context, Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {

    StringAggregatorConcatState state = (StringAggregatorConcatState) slateObject.get(this);
    state.sb.append(this.child.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean));
    return null;
  }

  @Override
  public String evaluateFinal(Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    StringAggregatorConcatState state = (StringAggregatorConcatState) slateObject.get(this);

    return state.sb.toString();
  }

  @Override
  public void reset() {
    // sb = new StringBuilder();
  }

  class StringAggregatorConcatState {
    StringBuilder sb = new StringBuilder();
  }

  @Override
  public void initSlate(Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    slateObject.put(this, new StringAggregatorConcatState());
  }

  @Override
  public void updateState(VarContext context, Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    // TODO Auto-generated method stub
    StringAggregatorConcatState state = (StringAggregatorConcatState) slateObject.get(this);
    state.sb.append(this.child.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean));

  }

}
