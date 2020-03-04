package com.adichad.lucense.expression.node.ints;

import java.util.List;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;

final public class IntMin implements IntExpressionNode {
  private final List<IntExpressionNode> children;

  public IntMin(List<IntExpressionNode> children) {
    this.children = children;
  }

  @Override
  final public int evaluate(Slates slates, Context cx) {
    int min = this.children.get(0).evaluate(slates, cx);
    for (int i = 1; i < this.children.size(); i++) {
      int curr = this.children.get(i).evaluate(slates, cx);
      if (curr < min)
        min = curr;
    }
    return min;
  }

  @Override
  final public int evaluateFinal(Slates slates, Context cx) {
    int min = this.children.get(0).evaluateFinal(slates, cx);
    for (int i = 1; i < this.children.size(); i++) {
      int curr = this.children.get(i).evaluateFinal(slates, cx);
      if (curr < min)
        min = curr;
    }
    return min;
  }
}
