package com.adichad.lucense.expression.node.floats;

import java.util.List;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;

public class FloatPlus implements FloatExpressionNode {
  List<FloatExpressionNode> children;

  public FloatPlus(List<FloatExpressionNode> children) {
    this.children = children;
  }

  @Override
  public float evaluate(Slates slates, Context cx) {
    float i = 0f;
    for (FloatExpressionNode child : this.children) {
      i += child.evaluate(slates, cx);
    }
    return i;
  }

  @Override
  public float evaluateFinal(Slates slates, Context cx) {
    float i = 0f;
    for (FloatExpressionNode child : this.children) {
      i += child.evaluateFinal(slates, cx);
    }
    return i;
  }

}
