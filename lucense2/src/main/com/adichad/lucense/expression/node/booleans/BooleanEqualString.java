package com.adichad.lucense.expression.node.booleans;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.strings.StringExpressionNode;

final public class BooleanEqualString implements BooleanExpressionNode {
  private final StringExpressionNode left;

  private final StringExpressionNode right;

  public BooleanEqualString(StringExpressionNode left, StringExpressionNode right) {
    this.left = left;
    this.right = right;
  }

  @Override
  final public boolean evaluate(Slates slates, Context cx) {
    return this.left.evaluate(slates, cx).equals(
        this.right.evaluate(slates, cx));
  }

  @Override
  final public boolean evaluateFinal(Slates slates, Context cx) {
    return this.left.evaluateFinal(slates, cx).equals(
        this.right.evaluateFinal(slates, cx));
  }

}
