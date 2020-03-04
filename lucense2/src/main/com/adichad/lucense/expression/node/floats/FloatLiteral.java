package com.adichad.lucense.expression.node.floats;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;

public class FloatLiteral implements FloatExpressionNode {
  float val;

  public FloatLiteral(float val) {
    this.val = val;
  }

  @Override
  public float evaluate(Slates slates, Context cx) {
    return this.val;
  }

  @Override
  public float evaluateFinal(Slates slates, Context cx) {
    return this.val;
  }

}
