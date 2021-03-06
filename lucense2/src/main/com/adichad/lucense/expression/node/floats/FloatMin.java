package com.adichad.lucense.expression.node.floats;

import java.util.List;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;

public class FloatMin implements FloatExpressionNode {
  List<FloatExpressionNode> children;

  public FloatMin(List<FloatExpressionNode> children) {
    this.children = children;
  }

  @Override
  public float evaluate(Slates slates, Context cx) {
    float min = this.children.get(0).evaluate(slates, cx);
    for (int i = 1; i < this.children.size(); i++) {
      float curr = this.children.get(i).evaluate(slates, cx);
      if (curr < min)
        min = curr;
    }
    return min;
  }

  @Override
  public float evaluateFinal(Slates slates, Context cx) {
    float min = this.children.get(0).evaluateFinal(slates, cx);
    for (int i = 1; i < this.children.size(); i++) {
      float curr = this.children.get(i).evaluateFinal(slates, cx);
      if (curr < min)
        min = curr;
    }
    return min;
  }
}
