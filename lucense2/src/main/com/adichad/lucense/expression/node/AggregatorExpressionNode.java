package com.adichad.lucense.expression.node;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;

public interface AggregatorExpressionNode extends ExpressionNode {
  
  public void initSlate(Slates slates, Context cx);

  public void updateState(Slates slates, Context cx);
}
