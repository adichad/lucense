package com.adichad.lucense.expression.node.doubles;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;

public class DoubleLiteral implements DoubleExpressionNode {
  double val;

  public DoubleLiteral(double val) {
    this.val = val;
  }

  @Override
  public double evaluate(Slates slates, Context cx) {
    return this.val;
  }

  @Override
  public double evaluateFinal(Slates slates, Context cx) {
    return this.val;
  }

}
