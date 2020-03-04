package com.adichad.lucense.expression.node.floats;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;
import com.adichad.lucense.expression.node.Variable;

public class FloatVariable implements AggregatorExpressionNode,
    FloatExpressionNode, Variable {
  String name;

  public FloatVariable(String name) {
    this.name = name;
  }

  @Override
  public float evaluate(Slates slates, Context cx) {
    return slates.context.floatVals.get(this.name);
  }

  @Override
  public float evaluateFinal(Slates slates, Context cx) {
    return slates.slateFloat.getFloat(this);
  }

  @Override
  public void initSlate(Slates slates, Context cx) {
  }

  @Override
  public void updateState(Slates slates, Context cx) {
    // float val = context.floatVals.get(name);
    // slates.slateFloat.put(this, val);
  }

}
