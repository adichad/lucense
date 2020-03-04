package com.adichad.lucense.expression.node.ints;

import java.util.List;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;

final public class IntPlus implements IntExpressionNode {
  private final List<IntExpressionNode> children;

  public IntPlus(List<IntExpressionNode> children) {
    this.children = children;
  }

  @Override
  final public int evaluate(Slates slates, Context cx) {
    int i = 0;
    for (IntExpressionNode child : this.children) {
      i += child.evaluate(slates, cx);
    }
    return i;
  }

  @Override
  final public int evaluateFinal(Slates slates, Context cx) {
    int i = 0;
    for (IntExpressionNode child : this.children) {
      i += child.evaluateFinal(slates, cx);
    }
    return i;
  }
}
