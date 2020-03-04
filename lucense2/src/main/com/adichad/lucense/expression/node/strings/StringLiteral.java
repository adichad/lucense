package com.adichad.lucense.expression.node.strings;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;

public class StringLiteral implements StringExpressionNode {
  String val;

  public StringLiteral(String val) {
    this.val = val;
  }

  @Override
  public String evaluate(Slates slates, Context cx) {
    return this.val;
  }

  @Override
  public String evaluateFinal(Slates slates, Context cx) {
    return this.val;
  }

}
