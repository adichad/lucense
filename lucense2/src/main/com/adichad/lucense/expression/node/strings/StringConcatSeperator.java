package com.adichad.lucense.expression.node.strings;

import java.util.Iterator;
import java.util.List;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;

public class StringConcatSeperator implements StringExpressionNode {
  List<StringExpressionNode>   children;

  private StringExpressionNode seperator;

  public StringConcatSeperator(StringExpressionNode seperator,
      List<StringExpressionNode> children) {
    this.seperator = seperator;
    this.children = children;
  }

  @Override
  public String evaluate(Slates slates, Context cx) {
    StringBuilder sb = new StringBuilder();
    Iterator<StringExpressionNode> iter = this.children.iterator();
    if (iter.hasNext()) {
      sb.append(iter.next().evaluate(slates, cx));
      if (iter.hasNext()) {
        String sep = this.seperator.evaluate(slates, cx);
        while (iter.hasNext()) {
          sb.append(sep).append(iter.next().evaluate(slates, cx));
        }
      }
    }
    return sb.toString();
  }

  @Override
  public String evaluateFinal(Slates slates, Context cx) {
    StringBuilder sb = new StringBuilder();
    Iterator<StringExpressionNode> iter = this.children.iterator();
    if (iter.hasNext()) {
      sb.append(iter.next().evaluateFinal(slates, cx));
      if (iter.hasNext()) {
        String sep = this.seperator.evaluateFinal(slates, cx);
        while (iter.hasNext()) {
          sb.append(sep).append(iter.next().evaluateFinal(slates, cx));
        }
      }
    }
    return sb.toString();
  }

}
