package com.adichad.lucense.expression.node.doubles;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;
import com.adichad.lucense.expression.node.Variable;

public class DoubleVariable implements AggregatorExpressionNode, DoubleExpressionNode, Variable {
  String name;

  public DoubleVariable(String name) {
    this.name = name;
  }

  @Override
  public double evaluate(Slates slates, Context cx) {
    return slates.context.doubleVals.get(this.name);
  }

  @Override
  public double evaluateFinal(Slates slates, Context cx) {
    return slates.slateDouble.getDouble(this);
  }

  @Override
  public void initSlate(Slates slates, Context cx) {

  }

  @Override
  public void updateState(Slates slates, Context cx) {
    // double val = context.doubleVals.get(name);
    // slateDouble.put(this, val);
  }

}
