package com.adichad.lucense.expression.node.floats;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;

public class FloatAggregatorMin implements AggregatorExpressionNode,
    FloatExpressionNode {
  FloatExpressionNode child;

  public FloatAggregatorMin(FloatExpressionNode child) {
    this.child = child;
  }

  @Override
  public float evaluate(Slates slates, Context cx) {
    float curr = this.child.evaluate(slates, cx);
    float min = slates.slateFloat.getFloat(this);
    if (min > curr)
      min = curr;
    slates.slateFloat.put(this, min);
    return min;
  }

  @Override
  public float evaluateFinal(Slates slates, Context cx) {
    return slates.slateFloat.get(this);
  }

  @Override
  public void initSlate(Slates slates, Context cx) {
    slates.slateFloat.put(this, Float.MAX_VALUE);
  }

  @Override
  public void updateState(Slates slates, Context cx) {

    float curr = this.child.evaluate(slates, cx);
    float min = slates.slateFloat.getFloat(this);
    if (min > curr)
      min = curr;
    slates.slateFloat.put(this, min);

  }

}
