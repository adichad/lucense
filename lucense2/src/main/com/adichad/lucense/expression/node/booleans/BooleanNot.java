package com.adichad.lucense.expression.node.booleans;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;

final public class BooleanNot implements BooleanExpressionNode {
  private final BooleanExpressionNode child;

  public BooleanNot(BooleanExpressionNode child) {
    this.child = child;
  }

  @Override
  final public boolean evaluate(Slates slates, Context cx) {
    return !this.child.evaluate(slates, cx);
  }

  @Override
  final public boolean evaluateFinal(Slates slates, Context cx) {
    return !this.child.evaluateFinal(slates, cx);
  }

}
