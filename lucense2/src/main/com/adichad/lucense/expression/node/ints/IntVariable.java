package com.adichad.lucense.expression.node.ints;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;
import com.adichad.lucense.expression.node.Variable;

final public class IntVariable implements AggregatorExpressionNode,
    IntExpressionNode, Variable {
  private final String name;

  public IntVariable(String name) {
    this.name = name;
  }

  @Override
  final public int evaluate(Slates slates, Context cx) {
    return slates.context.intVals.get(this.name);
  }

  @Override
  final public int evaluateFinal(Slates slates, Context cx) {
    return slates.slateInt.getInt(this);
  }


  @Override
  final public void initSlate(Slates slates, Context cx) {

  }

  @Override
  final public void updateState(Slates slates, Context cx) {
    // System.out.println("update called");
    // int val = context.intVals.get(name);
    // slateInt.put(this, val);
  }

}
