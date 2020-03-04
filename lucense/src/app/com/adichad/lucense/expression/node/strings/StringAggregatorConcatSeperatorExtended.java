package com.adichad.lucense.expression.node.strings;

import java.util.Map;

import com.adichad.lucense.expression.VarContext;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;
import com.adichad.lucense.expression.node.booleans.BooleanExpressionNode;
import com.adichad.lucense.expression.node.ints.IntExpressionNode;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class StringAggregatorConcatSeperatorExtended implements AggregatorExpressionNode, StringExpressionNode {
  StringExpressionNode child;

  // StringBuilder sb = new StringBuilder();
  StringExpressionNode seperator;

  private BooleanExpressionNode filter;

  private IntExpressionNode offset;

  private IntExpressionNode limit;

  // private int currPos;

  public StringAggregatorConcatSeperatorExtended(StringExpressionNode child, StringExpressionNode seperator,
      BooleanExpressionNode filter, IntExpressionNode offset, IntExpressionNode limit) {
    this.child = child;
    this.seperator = seperator;
    this.filter = filter;
    this.offset = offset;
    this.limit = limit;
  }

  @Override
  public String evaluate(VarContext context, Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    Integer off = this.offset.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
    Integer lim = this.limit.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean) + off;
    StringAggregatorConcatState state = (StringAggregatorConcatState) slateObject.get(this);
    if ((state.currPos >= off) && (state.currPos < lim)) {
      if (this.filter.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean)) {
        if (state.sb.length() > 0)
          state.sb.append(this.seperator
              .evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean));
        state.sb.append(this.child.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean));
        state.currPos++;
      }
    }
    return null;
  }

  @Override
  public String evaluateFinal(Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    return ((StringAggregatorConcatState) slateObject.get(this)).sb.toString();
  }

  @Override
  public void reset() {}

  class StringAggregatorConcatState {
    StringBuilder sb = new StringBuilder();

    int currPos = 0;
  }

  @Override
  public void initSlate(Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    // TODO Auto-generated method stub
    slateObject.put(this, new StringAggregatorConcatState());
  }

  @Override
  public void updateState(VarContext context, Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    // TODO Auto-generated method stub
    int off = this.offset.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
    int lim = this.limit.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean) + off;
    StringAggregatorConcatState state = (StringAggregatorConcatState) slateObject.get(this);
    if ((state.currPos >= off) && (state.currPos < lim)) {
      if (this.filter.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean)) {
        if (state.sb.length() > 0)
          state.sb.append(this.seperator
              .evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean));
        state.sb.append(this.child.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean));
        state.currPos++;
      }
    }
  }

}
