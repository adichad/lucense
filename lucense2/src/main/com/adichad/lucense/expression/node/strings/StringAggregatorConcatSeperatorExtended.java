package com.adichad.lucense.expression.node.strings;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;
import com.adichad.lucense.expression.node.booleans.BooleanExpressionNode;
import com.adichad.lucense.expression.node.ints.IntExpressionNode;

final public class StringAggregatorConcatSeperatorExtended implements
    AggregatorExpressionNode, StringExpressionNode {
  private final StringExpressionNode  child;

  private final StringExpressionNode  seperator;

  private final BooleanExpressionNode filter;

  private final IntExpressionNode     offset;

  private final IntExpressionNode     limit;

  public StringAggregatorConcatSeperatorExtended(StringExpressionNode child,
      StringExpressionNode seperator, BooleanExpressionNode filter,
      IntExpressionNode offset, IntExpressionNode limit) {
    this.child = child;
    this.seperator = seperator;
    this.filter = filter;
    this.offset = offset;
    this.limit = limit;
  }

  @Override
  final public String evaluate(Slates slates, Context cx) {
    Integer off = this.offset.evaluate(slates, cx);
    Integer lim = this.limit.evaluate(slates, cx) + off;
    StringAggregatorConcatState state = (StringAggregatorConcatState) slates.slateObject
        .get(this);
    if ((state.currPos >= off) && (state.currPos < lim)) {
      if (this.filter.evaluate(slates, cx)) {
        if (state.sb.length() > 0)
          state.sb.append(this.seperator.evaluate(slates, cx));
        state.sb.append(this.child.evaluate(slates, cx));
        state.currPos++;
      }
    }
    return null;
  }

  @Override
  final public String evaluateFinal(Slates slates, Context cx) {
    return ((StringAggregatorConcatState) slates.slateObject.get(this)).sb
        .toString();
  }

  private static class StringAggregatorConcatState {
    StringBuilder sb      = new StringBuilder();

    int           currPos = 0;
  }

  @Override
  final public void initSlate(Slates slates, Context cx) {
    // TODO Auto-generated method stub
    slates.slateObject.put(this, new StringAggregatorConcatState());
  }

  @Override
  final public void updateState(Slates slates, Context cx) {
    // TODO Auto-generated method stub
    int off = this.offset.evaluate(slates, cx);
    int lim = this.limit.evaluate(slates, cx) + off;
    StringAggregatorConcatState state = (StringAggregatorConcatState) slates.slateObject
        .get(this);
    if ((state.currPos >= off) && (state.currPos < lim)) {
      if (this.filter.evaluate(slates, cx)) {
        if (state.sb.length() > 0)
          state.sb.append(this.seperator.evaluate(slates, cx));
        state.sb.append(this.child.evaluate(slates, cx));
        state.currPos++;
      }
    }
  }

}
