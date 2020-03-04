package com.adichad.lucense.expression.node.booleans;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.ints.IntExpressionNode;

final public class BooleanNotEqualInt implements BooleanExpressionNode {
  private final IntExpressionNode left;

  private final IntExpressionNode right;

  public BooleanNotEqualInt(IntExpressionNode left, IntExpressionNode right) {
    this.left = left;
    this.right = right;
  }

  @Override
  final public boolean evaluate(Slates slates, Context cx) {
    return this.left.evaluate(slates, cx) != (this.right
        .evaluate(slates, cx));
  }

  @Override
  final public boolean evaluateFinal(Slates slates, Context cx) {
    return this.left.evaluateFinal(slates, cx) != (this.right
        .evaluateFinal(slates, cx));
  }
}
