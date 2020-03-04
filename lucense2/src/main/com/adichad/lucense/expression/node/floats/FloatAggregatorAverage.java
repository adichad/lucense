package com.adichad.lucense.expression.node.floats;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;

public class FloatAggregatorAverage implements AggregatorExpressionNode,
    FloatExpressionNode {
  FloatExpressionNode child;

  public FloatAggregatorAverage(FloatExpressionNode child) {
    this.child = child;
  }

  @Override
  public float evaluate(Slates slates, Context cx) {
    FloatAggregatorAverageSlate state = (FloatAggregatorAverageSlate) slates.slateObject
        .get(this);
    state.i += this.child.evaluate(slates, cx);
    state.n++;
    return 0;
  }

  @Override
  public float evaluateFinal(Slates slates, Context cx) {
    FloatAggregatorAverageSlate state = (FloatAggregatorAverageSlate) slates.slateObject
        .get(this);
    if (state.n == 0)
      return Float.NaN;
    return state.i / ((Integer) state.n).floatValue();
  }

  public class FloatAggregatorAverageSlate {
    float i = 0f;

    int   n = 0;
  }

  @Override
  public void initSlate(Slates slates, Context cx) {
    slates.slateObject.put(this, new FloatAggregatorAverageSlate());
  }

  @Override
  public void updateState(Slates slates, Context cx) {
    FloatAggregatorAverageSlate state = (FloatAggregatorAverageSlate) slates.slateObject
        .get(this);
    state.i += this.child.evaluate(slates, cx);
    state.n++;
  }

}
