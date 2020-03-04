package com.adichad.lucense.expression.node.booleans;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;
import com.adichad.lucense.expression.node.Variable;

final public class BooleanVariable implements AggregatorExpressionNode, BooleanExpressionNode, Variable {
  private final String name;

  public BooleanVariable(String name) {
    this.name = name;
  }

  @Override
  final public boolean evaluate(Slates slates, Context cx) {
    return slates.context.booleanVals.get(this.name);
  }

  @Override
  final public boolean evaluateFinal(Slates slates, Context cx) {
    return slates.slateBoolean.getBoolean(this);
  }

  @Override
  final public void initSlate(Slates slates, Context cx) {}

  @Override
  final public void updateState(Slates slates, Context cx) {
    // boolean val = context.booleanVals.get(name);
    // slateBoolean.put(this, val);
  }

}
