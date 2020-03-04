package com.adichad.lucense.expression.node.floats;

import java.util.List;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;

public class FloatMinus implements FloatExpressionNode {
  List<FloatExpressionNode> negChildren;

  FloatExpressionNode       posChild;

  public FloatMinus(FloatExpressionNode posChild,
      List<FloatExpressionNode> negChildren) {
    this.posChild = posChild;
    this.negChildren = negChildren;
  }

  @Override
  public float evaluate(Slates slates, Context cx) {
    float i = this.posChild.evaluate(slates, cx);
    for (FloatExpressionNode child : this.negChildren) {
      i -= child.evaluate(slates, cx);
    }
    return i;
  }

  @Override
  public float evaluateFinal(Slates slates, Context cx) {
    float i = this.posChild.evaluateFinal(slates, cx);
    for (FloatExpressionNode child : this.negChildren) {
      i -= child.evaluateFinal(slates, cx);
    }
    return i;
  }

}
