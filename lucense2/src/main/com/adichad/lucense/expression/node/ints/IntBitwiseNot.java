package com.adichad.lucense.expression.node.ints;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;

final public class IntBitwiseNot implements IntExpressionNode {
  private final IntExpressionNode dividend;

  public IntBitwiseNot(IntExpressionNode dividend) {
    this.dividend = dividend;
  }

  @Override
  final public int evaluate(Slates slates, Context cx) {
    return ~this.dividend.evaluate(slates, cx);
  }

  @Override
  final public int evaluateFinal(Slates slates, Context cx) {
    return ~this.dividend.evaluateFinal(slates, cx);
  }

}
