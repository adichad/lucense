package com.adichad.lucense.expression.node.ints;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.util.ArrayUtil;
import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;

final public class IntLinearStepperInt implements IntExpressionNode {
  private final IntExpressionNode var;
  private final int               slope;
  // private final boolean descending;
  // private final boolean excludeUpperBound;
  private final int[]             sentinels;
  private final int               intercept;

  public IntLinearStepperInt(IntExpressionNode var, int slope, int intercept,
  /* boolean descending, boolean excludeUpperBound, */int[] sentinels) {
    this.var = var;
    this.slope = slope;
    this.intercept = intercept;
    // this.descending = descending;
    // this.excludeUpperBound = excludeUpperBound;
    Integer[] arrTemp = ArrayUtils.toObject(sentinels);
    ArrayUtil.quickSort(arrTemp);
    int[] tempSentinels = ArrayUtils.toPrimitive(arrTemp);
    ArrayUtils.reverse(tempSentinels);
    this.sentinels = tempSentinels;
  }

  @Override
  final public int evaluate(Slates slates, Context cx) {
    int val = this.var.evaluate(slates, cx);
    for (int step = 0; step < sentinels.length; ++step)
      if (val > sentinels[step])
        return intercept + slope * step;
    return intercept + slope * sentinels.length;
  }

  @Override
  final public int evaluateFinal(Slates slates, Context cx) {
    int val = this.var.evaluate(slates, cx);
    for (int step = 0; step < sentinels.length; ++step)
      if (val < sentinels[step])
        return intercept + slope * step;
    return intercept + slope * sentinels.length;
  }

}
