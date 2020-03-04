package com.adichad.lucense.expression.node.strings;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.adichad.lucense.expression.VarContext;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class StringConcatSeperator implements StringExpressionNode {
  List<StringExpressionNode> children;

  private StringExpressionNode seperator;

  public StringConcatSeperator(StringExpressionNode seperator, List<StringExpressionNode> children) {
    this.seperator = seperator;
    this.children = children;
  }

  @Override
  public String evaluate(VarContext context, Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    StringBuilder sb = new StringBuilder();
    Iterator<StringExpressionNode> iter = this.children.iterator();
    if (iter.hasNext()) {
      sb.append(iter.next().evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean));
      if (iter.hasNext()) {
        String sep = this.seperator.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
        while (iter.hasNext()) {
          sb.append(sep).append(
              iter.next().evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean));
        }
      }
    }
    return sb.toString();
  }

  @Override
  public String evaluateFinal(Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    StringBuilder sb = new StringBuilder();
    Iterator<StringExpressionNode> iter = this.children.iterator();
    if (iter.hasNext()) {
      sb.append(iter.next().evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean));
      if (iter.hasNext()) {
        String sep = this.seperator.evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
        while (iter.hasNext()) {
          sb.append(sep)
              .append(iter.next().evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean));
        }
      }
    }
    return sb.toString();
  }

}
