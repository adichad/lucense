package com.adichad.lucense.expression.node.ints;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;

final public class IntRandom implements IntExpressionNode {
  private final IntExpressionNode from, to;

  public IntRandom(IntExpressionNode from, IntExpressionNode to) {
    this.from = from;
    this.to = to;
  }

  @Override
  final public int evaluate(Slates slates, Context cx) {
    int f = this.from.evaluate(slates, cx);
    return ((Double) (Math.random() * (this.to.evaluate(slates, cx) - f) + f))
        .intValue();
  }

  @Override
  final public int evaluateFinal(Slates slates, Context cx) {
    int f = this.from.evaluateFinal(slates, cx);
    return ((Double) (Math.random() * (this.to.evaluateFinal(slates, cx) - f) + f))
        .intValue();
  }

}
