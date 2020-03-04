package com.adichad.lucense.expression.node.floats;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;

public class FloatAggregatorSum implements AggregatorExpressionNode,
    FloatExpressionNode {
  FloatExpressionNode child;

  public FloatAggregatorSum(FloatExpressionNode child) {
    this.child = child;
  }

  @Override
  public float evaluate(Slates slates, Context cx) {
    float i = slates.slateFloat.get(this);
    i += this.child.evaluate(slates, cx);
    slates.slateFloat.put(this, i);
    return i;
  }

  @Override
  public float evaluateFinal(Slates slates, Context cx) {
    return slates.slateFloat.get(this);
  }

  @Override
  public void initSlate(Slates slates, Context cx) {
    slates.slateFloat.put(this, 0f);
  }

  @Override
  public void updateState(Slates slates, Context cx) {
    // TODO Auto-generated method stub
    float i = slates.slateFloat.get(this);
    i += this.child.evaluate(slates, cx);
    slates.slateFloat.put(this, i);
  }
}
