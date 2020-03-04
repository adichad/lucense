package com.adichad.lucense.expression.node.ints;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;

final public class IntSignedShiftLeft implements IntExpressionNode {
  private final IntExpressionNode shifted;

  private final IntExpressionNode shiftby;

  public IntSignedShiftLeft(IntExpressionNode shifted, IntExpressionNode shiftby) {
    this.shifted = shifted;
    this.shiftby = shiftby;
  }

  @Override
  final public int evaluate(Slates slates, Context cx) {
    return this.shifted.evaluate(slates, cx) << this.shiftby.evaluate(
        slates, cx);
  }

  @Override
  final public int evaluateFinal(Slates slates, Context cx) {
    return this.shifted.evaluateFinal(slates, cx) << this.shiftby
        .evaluateFinal(slates, cx);
  }

}
