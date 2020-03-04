package com.adichad.lucense.expression.node.booleans;

import java.util.List;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;

final public class BooleanInBoolean implements BooleanExpressionNode {
  private final List<BooleanExpressionNode> haystack;

  private final BooleanExpressionNode needle;

  public BooleanInBoolean(BooleanExpressionNode needle, List<BooleanExpressionNode> haystack) {
    this.needle = needle;
    this.haystack = haystack;
  }

  @Override
  final public boolean evaluate(Slates slates, Context cx) {
    boolean b = false;
    if (this.haystack.size() != 0) {
      boolean n = this.needle.evaluate(slates, cx);
      for (BooleanExpressionNode pin : this.haystack) {
        if (pin.evaluate(slates, cx) == n) {
          b = true;
          break;
        }
      }
    }
    return b;
  }

  @Override
  final public boolean evaluateFinal(Slates slates, Context cx) {
    Boolean b = false;
    if (this.haystack.size() != 0) {
      boolean n = this.needle.evaluateFinal(slates, cx);
      for (BooleanExpressionNode pin : this.haystack) {
        if (pin.evaluateFinal(slates, cx) == n) {
          b = true;
          break;
        }
      }
    }
    return b;
  }

}
