package com.adichad.lucense.expression.node.booleans;

import java.util.List;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.doubles.DoubleExpressionNode;

final public class BooleanInDouble implements BooleanExpressionNode {
  private final List<DoubleExpressionNode> haystack;

  private final DoubleExpressionNode needle;

  public BooleanInDouble(DoubleExpressionNode needle, List<DoubleExpressionNode> haystack) {
    this.needle = needle;
    this.haystack = haystack;
  }

  @Override
  final public boolean evaluate(Slates slates, Context cx) {
    boolean b = false;
    if (this.haystack.size() != 0) {
      double n = this.needle.evaluate(slates, cx);
      for (DoubleExpressionNode pin : this.haystack) {
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
      double n = this.needle.evaluateFinal(slates, cx);
      for (DoubleExpressionNode pin : this.haystack) {
        if (pin.evaluateFinal(slates, cx) == n) {
          b = true;
          break;
        }
      }
    }
    return b;
  }

}
