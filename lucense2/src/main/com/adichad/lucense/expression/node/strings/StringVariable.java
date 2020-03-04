package com.adichad.lucense.expression.node.strings;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;
import com.adichad.lucense.expression.node.Variable;

public class StringVariable implements AggregatorExpressionNode, StringExpressionNode, Variable {
  String name;

  public StringVariable(String name) {
    this.name = name;
  }

  @Override
  public void initSlate(Slates slates, Context cx) {}

  @Override
  public String evaluate(Slates slates, Context cx) {
    return slates.context.stringVals.get(this.name);
  }

  @Override
  public String evaluateFinal(Slates slates, Context cx) {
    return (String) slates.slateObject.get(this);
  }

  @Override
  public void updateState(Slates slates, Context cx) {
    String val = slates.context.stringVals.get(this.name);
    slates.slateObject.put(this, val);
  }

}
