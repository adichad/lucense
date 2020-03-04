package com.adichad.lucense.expression.node.strings;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;

final public class StringAggregatorConcatSeperator implements AggregatorExpressionNode, StringExpressionNode {
  StringExpressionNode child;

  // StringBuilder sb = new StringBuilder();
  private final StringExpressionNode seperator;

  public StringAggregatorConcatSeperator(StringExpressionNode child, StringExpressionNode seperator) {
    this.child = child;
    this.seperator = seperator;
  }

  @Override
  final public String evaluate(Slates slates, Context cx) {
    StringAggregatorConcatSeperatorState state = (StringAggregatorConcatSeperatorState) slates.slateObject.get(this);
    if (state.sb.length() > 0)
      state.sb.append(this.seperator.evaluate(slates, cx));
    state.sb.append(this.child.evaluate(slates, cx));
    return null;
  }

  @Override
  final public String evaluateFinal(Slates slates, Context cx) {
    StringAggregatorConcatSeperatorState state = (StringAggregatorConcatSeperatorState) slates.slateObject.get(this);
    return state.sb.toString();
  }

  private static class StringAggregatorConcatSeperatorState {
    StringBuilder sb = new StringBuilder();
  }

  @Override
  final public void initSlate(Slates slates, Context cx) {
    slates.slateObject.put(this, new StringAggregatorConcatSeperatorState());
  }

  @Override
  final public void updateState(Slates slates, Context cx) {
    StringAggregatorConcatSeperatorState state = (StringAggregatorConcatSeperatorState) slates.slateObject.get(this);
    if (state.sb.length() > 0)
      state.sb.append(this.seperator.evaluate(slates, cx));
    state.sb.append(this.child.evaluate(slates, cx));
  }

}
