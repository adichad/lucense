package com.adichad.lucense.expression.node.floats;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;

public class FloatDivide implements FloatExpressionNode {
  private FloatExpressionNode dividend;

  private FloatExpressionNode divisor;

  public FloatDivide(FloatExpressionNode dividend, FloatExpressionNode divisor) {
    this.dividend = dividend;
    this.divisor = divisor;
  }

  @Override
  public float evaluate(Slates slates, Context cx) {
    return this.dividend.evaluate(slates, cx)
        / this.divisor.evaluate(slates, cx);
  }

  @Override
  public float evaluateFinal(Slates slates, Context cx) {
    return this.dividend.evaluateFinal(slates, cx)
        / this.divisor.evaluateFinal(slates, cx);
  }

}
