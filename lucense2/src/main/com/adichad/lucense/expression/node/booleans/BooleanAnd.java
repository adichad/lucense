package com.adichad.lucense.expression.node.booleans;

import java.util.List;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;

final public class BooleanAnd implements BooleanExpressionNode {
  private final List<BooleanExpressionNode> children;

  public BooleanAnd(List<BooleanExpressionNode> children) {
    this.children = children;
  }

  @Override
  final public boolean evaluate(Slates slates, Context cx) {
    for (BooleanExpressionNode child : this.children) {
      if (!child.evaluate(slates, cx))
        return false;
    }
    return true;
  }

  @Override
  final public boolean evaluateFinal(Slates slates, Context cx) {
    for (BooleanExpressionNode child : this.children) {
      if (!child.evaluateFinal(slates, cx))
        return false;
    }
    return true;
  }

}
