package com.adichad.lucense.expression.node.ints;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;

final public class IntAggregatorMin implements AggregatorExpressionNode,
    IntExpressionNode {
  private final IntExpressionNode child;

  public IntAggregatorMin(IntExpressionNode child) {
    this.child = child;
  }

  @Override
  final public int evaluate(Slates slates, Context cx) {
    int curr = this.child.evaluate(slates, cx);
    int min = slates.slateInt.get(this);
    if (min > curr)
      min = curr;
    slates.slateInt.put(this, min);
    return min;
  }

  @Override
  final public int evaluateFinal(Slates slates, Context cx) {
    return slates.slateInt.get(this);
  }

  @Override
  final public void initSlate(Slates slates, Context cx) {
    slates.slateInt.put(this, Integer.MAX_VALUE);
  }

  @Override
  final public void updateState(Slates slates, Context cx) {
    int curr = this.child.evaluate(slates, cx);
    int min = slates.slateInt.get(this);
    if (min > curr)
      min = curr;
    slates.slateInt.put(this, min);

  }

}
