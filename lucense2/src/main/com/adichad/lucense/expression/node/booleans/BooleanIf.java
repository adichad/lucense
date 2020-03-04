package com.adichad.lucense.expression.node.booleans;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;

final public class BooleanIf implements BooleanExpressionNode {
  private final BooleanExpressionNode condition;

  private final BooleanExpressionNode nthen;

  private final BooleanExpressionNode nelse;

  public BooleanIf(BooleanExpressionNode condition, BooleanExpressionNode nthen, BooleanExpressionNode nelse) {
    this.condition = condition;
    this.nthen = nthen;
    this.nelse = nelse;
  }

  @Override
  final public boolean evaluate(Slates slates, Context cx) {
    if (this.condition.evaluate(slates, cx))
      return this.nthen.evaluate(slates, cx);
    return this.nelse.evaluate(slates, cx);
  }

  @Override
  final public boolean evaluateFinal(Slates slates, Context cx) {
    if (this.condition.evaluateFinal(slates, cx))
      return this.nthen.evaluateFinal(slates, cx);
    return this.nelse.evaluateFinal(slates, cx);
  }

}
