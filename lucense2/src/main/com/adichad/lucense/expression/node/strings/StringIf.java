package com.adichad.lucense.expression.node.strings;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.booleans.BooleanExpressionNode;

public class StringIf implements StringExpressionNode {
  BooleanExpressionNode condition;

  StringExpressionNode  nthen;

  StringExpressionNode  nelse;

  public StringIf(BooleanExpressionNode condition, StringExpressionNode nthen,
      StringExpressionNode nelse) {
    this.condition = condition;
    this.nthen = nthen;
    this.nelse = nelse;
  }

  @Override
  public String evaluate(Slates slates, Context cx) {
    if (this.condition.evaluate(slates, cx))
      return this.nthen.evaluate(slates, cx);
    return this.nelse.evaluate(slates, cx);

  }

  @Override
  public String evaluateFinal(Slates slates, Context cx) {
    if (this.condition.evaluateFinal(slates, cx))
      return this.nthen.evaluateFinal(slates, cx);
    return this.nelse.evaluateFinal(slates, cx);
  }

}
