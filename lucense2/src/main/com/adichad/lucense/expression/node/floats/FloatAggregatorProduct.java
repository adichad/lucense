package com.adichad.lucense.expression.node.floats;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;

public class FloatAggregatorProduct implements AggregatorExpressionNode,
    FloatExpressionNode {
  FloatExpressionNode child;

  public FloatAggregatorProduct(FloatExpressionNode child) {
    this.child = child;
  }

  @Override
  public float evaluate(Slates slates, Context cx) {
    float i = slates.slateFloat.get(this);
    i *= this.child.evaluate(slates, cx);
    slates.slateFloat.put(this, i);
    return i;
  }

  @Override
  public float evaluateFinal(Slates slates, Context cx) {
    return slates.slateFloat.get(this);
  }

  @Override
  public void initSlate(Slates slates, Context cx) {
    slates.slateFloat.put(this, 1f);
  }

  @Override
  public void updateState(Slates slates, Context cx) {

    float i = slates.slateFloat.get(this);
    i *= this.child.evaluate(slates, cx);
    slates.slateFloat.put(this, i);
  }
}
