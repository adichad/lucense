package com.adichad.lucense.expression.node.ints;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;

final public class IntBitwiseOr implements IntExpressionNode {
  private final IntExpressionNode dividend;

  private final IntExpressionNode divisor;

  public IntBitwiseOr(IntExpressionNode dividend, IntExpressionNode divisor) {
    this.dividend = dividend;
    this.divisor = divisor;
  }

  @Override
  final public int evaluate(Slates slates, Context cx) {
    return this.dividend.evaluate(slates, cx)
        | this.divisor.evaluate(slates, cx);
  }

  @Override
  final public int evaluateFinal(Slates slates, Context cx) {
    return this.dividend.evaluateFinal(slates, cx)
        | this.divisor.evaluateFinal(slates, cx);
  }

}
