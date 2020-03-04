package com.adichad.lucense.expression.node.ints;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.strings.StringExpressionNode;

final public class IntLengthString implements IntExpressionNode {
  private final StringExpressionNode child;

  public IntLengthString(StringExpressionNode child) {
    this.child = child;
  }

  @Override
  final public int evaluate(Slates slates, Context cx) {
    return this.child.evaluate(slates, cx).length();
  }

  @Override
  final public int evaluateFinal(Slates slates, Context cx) {
    return this.child.evaluateFinal(slates, cx).length();
  }

}
