package com.adichad.lucense.expression.node.floats;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.booleans.BooleanExpressionNode;

public class FloatIf implements FloatExpressionNode {
  BooleanExpressionNode condition;

  FloatExpressionNode   nthen;

  FloatExpressionNode   nelse;

  public FloatIf(BooleanExpressionNode condition, FloatExpressionNode nthen,
      FloatExpressionNode nelse) {
    this.condition = condition;
    this.nthen = nthen;
    this.nelse = nelse;
  }

  @Override
  public float evaluate(Slates slates, Context cx) {
    if (this.condition.evaluate(slates, cx))
      return this.nthen.evaluate(slates, cx);
    return this.nelse.evaluate(slates, cx);
  }

  @Override
  public float evaluateFinal(Slates slates, Context cx) {
    if (this.condition.evaluateFinal(slates, cx))
      return this.nthen.evaluateFinal(slates, cx);
    return this.nelse.evaluateFinal(slates, cx);
  }

}
