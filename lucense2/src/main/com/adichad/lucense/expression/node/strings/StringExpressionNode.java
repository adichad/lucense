package com.adichad.lucense.expression.node.strings;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.ExpressionNode;

public interface StringExpressionNode extends ExpressionNode {
  public abstract String evaluate(Slates slates, Context cx);

  public abstract String evaluateFinal(Slates slates, Context cx);
}
