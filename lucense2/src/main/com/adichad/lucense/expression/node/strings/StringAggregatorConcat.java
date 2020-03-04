package com.adichad.lucense.expression.node.strings;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;

final public class StringAggregatorConcat implements AggregatorExpressionNode, StringExpressionNode {
  private final StringExpressionNode child;

  // StringBuilder sb = new StringBuilder();

  public StringAggregatorConcat(StringExpressionNode child) {
    this.child = child;
  }

  @Override
  final public String evaluate(Slates slates, Context cx) {

    StringAggregatorConcatState state = (StringAggregatorConcatState) slates.slateObject.get(this);
    state.sb.append(this.child.evaluate(slates, cx));
    return null;
  }

  @Override
  final public String evaluateFinal(Slates slates, Context cx) {
    StringAggregatorConcatState state = (StringAggregatorConcatState) slates.slateObject.get(this);

    return state.sb.toString();
  }

  final class StringAggregatorConcatState {
    StringBuilder sb = new StringBuilder();
  }

  @Override
  final public void initSlate(Slates slates, Context cx) {
    slates.slateObject.put(this, new StringAggregatorConcatState());
  }

  @Override
  final public void updateState(Slates slates, Context cx) {
    // TODO Auto-generated method stub
    StringAggregatorConcatState state = (StringAggregatorConcatState) slates.slateObject.get(this);
    state.sb.append(this.child.evaluate(slates, cx));

  }

}
