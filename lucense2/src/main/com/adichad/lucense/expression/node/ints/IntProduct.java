package com.adichad.lucense.expression.node.ints;

import java.util.List;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;

final public class IntProduct implements IntExpressionNode {
  private final List<IntExpressionNode> children;

  public IntProduct(List<IntExpressionNode> children) {
    this.children = children;
  }

  @Override
  final public int evaluate(Slates slates, Context cx) {
    int i = 1;
    for (IntExpressionNode child : this.children) {
      i *= child.evaluate(slates, cx);
      if (i == 0)
        return 0;
    }
    return i;
  }

  @Override
  final public int evaluateFinal(Slates slates, Context cx) {
    int i = 1;
    for (IntExpressionNode child : this.children) {
      i *= child.evaluateFinal(slates, cx);
      if (i == 0)
        return 0;
    }
    return i;
  }
}
