package com.adichad.lucense.expression.node.ints;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.ExpressionNode;

public interface IntExpressionNode extends ExpressionNode {
  public abstract int evaluate(Slates slates, Context cx);

  public abstract int evaluateFinal(Slates slates, Context cx);
}
