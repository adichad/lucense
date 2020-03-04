package com.adichad.lucense.expression.node.floats;

import java.util.List;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;

public class FloatProduct implements FloatExpressionNode {
  List<FloatExpressionNode> children;

  public FloatProduct(List<FloatExpressionNode> children) {
    this.children = children;
  }

  @Override
  public float evaluate(Slates slates, Context cx) {
    float i = 1f;
    for (FloatExpressionNode child : this.children) {
      i *= child.evaluate(slates, cx);
      if (i == 0)
        return 0f;
    }
    return i;
  }

  @Override
  public float evaluateFinal(Slates slates, Context cx) {
    float i = 1f;
    for (FloatExpressionNode child : this.children) {
      i *= child.evaluateFinal(slates, cx);
      if (i == 0)
        return 0f;
    }
    return i;
  }
}
