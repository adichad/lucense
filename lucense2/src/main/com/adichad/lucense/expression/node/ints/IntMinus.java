package com.adichad.lucense.expression.node.ints;

import java.util.List;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;

final public class IntMinus implements IntExpressionNode {
  private final IntExpressionNode       positiveChild;

  private final List<IntExpressionNode> negativeChildren;

  public IntMinus(IntExpressionNode positiveChild,
      List<IntExpressionNode> negativeChildren) {
    this.positiveChild = positiveChild;
    this.negativeChildren = negativeChildren;
  }

  @Override
  final public int evaluate(Slates slates, Context cx) {
    int i = this.positiveChild.evaluate(slates, cx);
    for (IntExpressionNode child : this.negativeChildren) {
      i -= child.evaluate(slates, cx);
    }
    return i;
  }

  @Override
  final public int evaluateFinal(Slates slates, Context cx) {
    int i = this.positiveChild.evaluateFinal(slates, cx);
    for (IntExpressionNode child : this.negativeChildren) {
      i -= child.evaluateFinal(slates, cx);
    }
    return i;
  }

}
