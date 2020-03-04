package com.adichad.lucense.expression.node.strings;

import java.util.List;
import java.util.Map;

import com.adichad.lucense.expression.VarContext;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;
import com.adichad.lucense.expression.node.booleans.BooleanExpressionNode;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class StringIn implements BooleanExpressionNode {
  private final List<BooleanExpressionNode> haystack;

  private final BooleanExpressionNode needle;

  public StringIn(BooleanExpressionNode needle, List<BooleanExpressionNode> haystack) {
    this.needle = needle;
    this.haystack = haystack;
  }

  @Override
  public boolean evaluate(VarContext context, Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    boolean b = false;
    if (this.haystack.size() != 0) {
      boolean n = this.needle.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
      for (BooleanExpressionNode pin : this.haystack) {
        if (pin.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean) == n) {
          b = true;
          break;
        }
      }
    }
    return b;
  }

  @Override
  public boolean evaluateFinal(Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    Boolean b = false;
    if (this.haystack.size() != 0) {
      boolean n = this.needle.evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
      for (BooleanExpressionNode pin : this.haystack) {
        if (pin.evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean) == n) {
          b = true;
          break;
        }
      }
    }
    return b;
  }

}
