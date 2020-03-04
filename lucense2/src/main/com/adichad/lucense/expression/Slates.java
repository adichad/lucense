package com.adichad.lucense.expression;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.HashMap;
import java.util.Map;

import com.adichad.lucense.expression.node.AggregatorExpressionNode;

public class Slates {
  public final Map<AggregatorExpressionNode, Object>               slateObject;
  public final Object2IntOpenHashMap<AggregatorExpressionNode>     slateInt;
  public final Object2FloatOpenHashMap<AggregatorExpressionNode>   slateFloat;
  public final Object2DoubleOpenHashMap<AggregatorExpressionNode>  slateDouble;
  public final Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean;
  public final VarContext                                          context;

  Slates() {
    this.slateObject = new HashMap<AggregatorExpressionNode, Object>();
    this.slateInt = new Object2IntOpenHashMap<AggregatorExpressionNode>();
    this.slateFloat = new Object2FloatOpenHashMap<AggregatorExpressionNode>();
    this.slateDouble = new Object2DoubleOpenHashMap<AggregatorExpressionNode>();
    this.slateBoolean = new Object2BooleanOpenHashMap<AggregatorExpressionNode>();
    this.context = new VarContext();
  }

  Slates(int i) {
    this.slateObject = new HashMap<AggregatorExpressionNode, Object>(i);
    this.slateInt = new Object2IntOpenHashMap<AggregatorExpressionNode>(i);
    this.slateFloat = new Object2FloatOpenHashMap<AggregatorExpressionNode>(i);
    this.slateDouble = new Object2DoubleOpenHashMap<AggregatorExpressionNode>(i);
    this.slateBoolean = new Object2BooleanOpenHashMap<AggregatorExpressionNode>(
        i);
    this.context = new VarContext();
  }

  public void clear() {
    this.slateObject.clear();
    this.slateInt.clear();
    this.slateFloat.clear();
    this.slateDouble.clear();
    this.slateBoolean.clear();
  }
  
  public void setIntVariableValue(String name, int value) {
    this.context.intVals.put(name, value);
  }

  public void setFloatVariableValue(String name, float value) {
    this.context.floatVals.put(name, value);
  }

  public void setDoubleVariableValue(String name, double value) {
    this.context.doubleVals.put(name, value);
  }

  public void setBooleanVariableValue(String name, boolean value) {
    this.context.booleanVals.put(name, value);
  }

  public void setStringVariableValue(String name, String value) {
    this.context.stringVals.put(name, value);
  }


}