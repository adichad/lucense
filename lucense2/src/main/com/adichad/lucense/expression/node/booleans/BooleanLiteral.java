package com.adichad.lucense.expression.node.booleans;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;

final public class BooleanLiteral implements BooleanExpressionNode {
  private final boolean val;

  public BooleanLiteral(boolean val) {
    this.val = val;
  }

  @Override
  public boolean evaluate(Slates slates, Context cx) {
    return this.val;
  }

  @Override
  public boolean evaluateFinal(Slates slates, Context cx) {
    return this.val;
  }

}
