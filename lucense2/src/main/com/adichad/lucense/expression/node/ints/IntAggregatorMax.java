package com.adichad.lucense.expression.node.ints;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;

final public class IntAggregatorMax implements AggregatorExpressionNode, IntExpressionNode {
  private final IntExpressionNode child;

  public IntAggregatorMax(IntExpressionNode child) {
    this.child = child;
  }

  @Override
  final public int evaluate(Slates slates, Context cx) {
    int curr = this.child.evaluate(slates, cx);
    int max = slates.slateInt.getInt(this);
    if (max < curr)
      max = curr;
    slates.slateInt.put(this, max);
    return max;
  }

  @Override
  final public int evaluateFinal(Slates slates, Context cx) {
    return slates.slateInt.getInt(this);
  }

  @Override
  final public void initSlate(Slates slates, Context cx) {
    slates.slateInt.put(this, Integer.MIN_VALUE);
  }

  @Override
  final public void updateState(Slates slates, Context cx) {
    int curr = this.child.evaluate(slates, cx);
    int max = slates.slateInt.getInt(this);
    if (max < curr)
      max = curr;
    slates.slateInt.put(this, max);
  }

}
