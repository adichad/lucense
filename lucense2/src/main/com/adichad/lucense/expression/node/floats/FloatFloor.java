package com.adichad.lucense.expression.node.floats;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;

public class FloatFloor implements FloatExpressionNode {
  FloatExpressionNode child;

  public FloatFloor(FloatExpressionNode child) {
    this.child = child;
  }

  @Override
  public float evaluate(Slates slates, Context cx) {
    return ((Double) Math.floor(((Float) this.child.evaluate(slates, cx)).doubleValue())).floatValue();
  }

  @Override
  public float evaluateFinal(Slates slates, Context cx) {
    return ((Double) Math.floor(((Float) this.child.evaluateFinal(slates, cx))
        .doubleValue())).floatValue();
  }

}
