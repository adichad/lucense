package com.adichad.lucense.expression.node.booleans;

import java.util.List;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.strings.StringExpressionNode;

final public class BooleanInString implements BooleanExpressionNode {
  private final List<StringExpressionNode> haystack;

  private final StringExpressionNode needle;

  public BooleanInString(StringExpressionNode needle, List<StringExpressionNode> haystack) {
    this.needle = needle;
    this.haystack = haystack;
  }

  @Override
  final public boolean evaluate(Slates slates, Context cx) {
    boolean b = false;
    if (this.haystack.size() != 0) {
      String n = this.needle.evaluate(slates, cx);
      for (StringExpressionNode pin : this.haystack) {
        if (pin.evaluate(slates, cx).equals(n)) {
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
      String n = this.needle.evaluateFinal(slates, cx);
      for (StringExpressionNode pin : this.haystack) {
        if (pin.evaluateFinal(slates, cx).equals(n)) {
          b = true;
          break;
        }
      }
    }
    return b;
  }

}
