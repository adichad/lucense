package com.adichad.lucense.expression.node.booleans;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.ExpressionNode;

public interface BooleanExpressionNode extends ExpressionNode {
  public abstract boolean evaluate(Slates slates, Context cx);

  public abstract boolean evaluateFinal(Slates slates, Context cx);
}
