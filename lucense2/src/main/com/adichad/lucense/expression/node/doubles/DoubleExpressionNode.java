package com.adichad.lucense.expression.node.doubles;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.ExpressionNode;

public interface DoubleExpressionNode extends ExpressionNode {
  public abstract double evaluate(Slates slates, Context cx);

  public abstract double evaluateFinal(Slates slates, Context cx);
}
