package com.adichad.lucense.expression.node.ints;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.booleans.BooleanExpressionNode;

final public class IntIf implements IntExpressionNode {
  private final BooleanExpressionNode condition;

  private final IntExpressionNode     nthen;

  private final IntExpressionNode     nelse;

  public IntIf(BooleanExpressionNode condition, IntExpressionNode nthen,
      IntExpressionNode nelse) {
    this.condition = condition;
    this.nthen = nthen;
    this.nelse = nelse;
  }

  @Override
  final public int evaluate(Slates slates, Context cx) {
    if (this.condition.evaluate(slates, cx))
      return this.nthen.evaluate(slates, cx);
    return this.nelse.evaluate(slates, cx);
  }

  @Override
  final public int evaluateFinal(Slates slates, Context cx) {
    if (this.condition.evaluateFinal(slates, cx))
      return this.nthen.evaluateFinal(slates, cx);
    return this.nelse.evaluateFinal(slates, cx);
  }

}
