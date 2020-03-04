package com.adichad.lucense.expression.node.floats;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;

public class FloatAggregatorMax implements AggregatorExpressionNode,
    FloatExpressionNode {
  FloatExpressionNode child;

  public FloatAggregatorMax(FloatExpressionNode child) {
    this.child = child;
  }

  @Override
  public float evaluate(Slates slates, Context cx) {
    float curr = this.child.evaluate(slates, cx);
    float max = slates.slateFloat.getFloat(this);
    if (max < curr)
      max = curr;
    slates.slateFloat.put(this, max);
    return max;
  }

  @Override
  public float evaluateFinal(Slates slates, Context cx) {
    return slates.slateFloat.getFloat(this);
  }

  @Override
  public void initSlate(Slates slates, Context cx) {
    slates.slateFloat.put(this, Float.MIN_VALUE);
  }

  @Override
  public void updateState(Slates slates, Context cx) {
    float curr = this.child.evaluate(slates, cx);
    float max = slates.slateFloat.getFloat(this);
    if (max < curr)
      max = curr;
    slates.slateFloat.put(this, max);
  }

}
