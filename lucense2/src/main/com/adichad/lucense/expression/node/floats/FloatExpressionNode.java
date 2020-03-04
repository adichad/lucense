package com.adichad.lucense.expression.node.floats;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.ExpressionNode;

public interface FloatExpressionNode extends ExpressionNode {
  public abstract float evaluate(Slates slates, Context cx);

  public abstract float evaluateFinal(Slates slates, Context cx);
}
