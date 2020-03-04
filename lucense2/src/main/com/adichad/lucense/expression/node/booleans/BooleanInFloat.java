package com.adichad.lucense.expression.node.booleans;

import java.util.List;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.floats.FloatExpressionNode;

final public class BooleanInFloat implements BooleanExpressionNode {
  private final List<FloatExpressionNode> haystack;

  private final FloatExpressionNode needle;

  public BooleanInFloat(FloatExpressionNode needle, List<FloatExpressionNode> haystack) {
    this.needle = needle;
    this.haystack = haystack;
  }

  @Override
  final public boolean evaluate(Slates slates, Context cx) {
    boolean b = false;
    if (this.haystack.size() != 0) {
      float n = this.needle.evaluate(slates, cx);
      for (FloatExpressionNode pin : this.haystack) {
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
      float n = this.needle.evaluateFinal(slates, cx);
      for (FloatExpressionNode pin : this.haystack) {
        if (pin.evaluateFinal(slates, cx) == n) {
          b = true;
          break;
        }
      }
    }
    return b;
  }

}
