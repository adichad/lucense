package com.adichad.lucense.expression.node.strings;

import java.util.List;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.booleans.BooleanExpressionNode;

public class StringIn implements BooleanExpressionNode {
  private final List<BooleanExpressionNode> haystack;

  private final BooleanExpressionNode       needle;

  public StringIn(BooleanExpressionNode needle,
      List<BooleanExpressionNode> haystack) {
    this.needle = needle;
    this.haystack = haystack;
  }

  @Override
  public boolean evaluate(Slates slates, Context cx) {
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
  public boolean evaluateFinal(Slates slates, Context cx) {
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
