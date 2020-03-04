package com.adichad.lucense.expression.node.ints;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;

final public class IntLiteral implements IntExpressionNode {
  private final int val;

  public IntLiteral(int val) {
    this.val = val;
  }

  @Override
  final public int evaluate(Slates slates, Context cx) {
    return this.val;
  }

  @Override
  final public int evaluateFinal(Slates slates, Context cx) {
    return this.val;
  }

}
