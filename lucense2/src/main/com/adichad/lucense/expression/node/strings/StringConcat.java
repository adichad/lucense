package com.adichad.lucense.expression.node.strings;

import java.util.List;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;

public class StringConcat implements StringExpressionNode {
  List<StringExpressionNode> children;

  public StringConcat(List<StringExpressionNode> children) {
    this.children = children;
  }

  @Override
  public String evaluate(Slates slates, Context cx) {
    StringBuilder sb = new StringBuilder();
    for (StringExpressionNode child : this.children) {
      sb.append(child.evaluate(slates, cx));
    }
    return sb.toString();
  }

  @Override
  public String evaluateFinal(Slates slates, Context cx) {
    StringBuilder sb = new StringBuilder();
    for (StringExpressionNode child : this.children) {
      sb.append(child.evaluateFinal(slates, cx));
    }
    return sb.toString();
  }

}
