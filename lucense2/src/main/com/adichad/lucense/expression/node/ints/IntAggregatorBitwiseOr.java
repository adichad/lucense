package com.adichad.lucense.expression.node.ints;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;

public class IntAggregatorBitwiseOr implements AggregatorExpressionNode,
    IntExpressionNode {
  private final IntExpressionNode child;

  public IntAggregatorBitwiseOr(IntExpressionNode child) {
    this.child = child;
  }

  @Override
  final public int evaluate(Slates slates, Context cx) {
    int i = slates.slateInt.get(this);
    i |= this.child.evaluate(slates, cx);
    slates.slateInt.put(this, i);
    return i;
  }

  @Override
  final public int evaluateFinal(Slates slates, Context cx) {
    return slates.slateInt.getInt(this);
  }

  @Override
  final public void initSlate(Slates slates, Context cx) {
    slates.slateInt.put(this, 0);
  }

  @Override
  final public void updateState(Slates slates, Context cx) {
    int i = slates.slateInt.get(this);
    i |= this.child.evaluate(slates, cx);
    slates.slateInt.put(this, i);
  }
}
