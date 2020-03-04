package com.adichad.lucense.expression;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.adichad.lucense.expression.node.AggregatorExpressionNode;
import com.adichad.lucense.expression.node.ExpressionNode;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public abstract class ExpressionTree implements Cloneable {
  protected ExpressionNode root;

  protected VarContext context;

  protected Map<AggregatorExpressionNode, Object> slateObject;

  protected Object2IntOpenHashMap<AggregatorExpressionNode> slateInt;

  protected Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat;

  protected Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble;

  protected Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean;

  protected Set<String> intVars;

  protected Set<String> floatVars;

  protected Set<String> doubleVars;

  protected Set<String> booleanVars;

  protected Set<String> stringVars;

  public ExpressionTree(ExpressionNode root, Set<String> intVars, Set<String> floatVars, Set<String> doubleVars,
      Set<String> booleanVars, Set<String> stringVars) {
    this.root = root;
    this.intVars = intVars;
    this.floatVars = floatVars;
    this.doubleVars = doubleVars;
    this.booleanVars = booleanVars;
    this.stringVars = stringVars;

    this.context = new VarContext();

    this.slateObject = new HashMap<AggregatorExpressionNode, Object>();
    this.slateInt = new Object2IntOpenHashMap<AggregatorExpressionNode>();
    this.slateFloat = new Object2FloatOpenHashMap<AggregatorExpressionNode>();
    this.slateDouble = new Object2DoubleOpenHashMap<AggregatorExpressionNode>();
    this.slateBoolean = new Object2BooleanOpenHashMap<AggregatorExpressionNode>();
  }

  protected ExpressionTree(ExpressionTree tree) {
    this.root = tree.root;
    this.intVars = tree.intVars;
    this.floatVars = tree.floatVars;
    this.doubleVars = tree.doubleVars;
    this.booleanVars = tree.booleanVars;
    this.stringVars = tree.stringVars;
    this.context = tree.context;

    this.slateObject = new HashMap<AggregatorExpressionNode, Object>();
    this.slateInt = new Object2IntOpenHashMap<AggregatorExpressionNode>();
    this.slateFloat = new Object2FloatOpenHashMap<AggregatorExpressionNode>();
    this.slateDouble = new Object2DoubleOpenHashMap<AggregatorExpressionNode>();
    this.slateBoolean = new Object2BooleanOpenHashMap<AggregatorExpressionNode>();
  }

  public Set<String> getIntVariables() {
    return this.intVars;
  }

  public Set<String> getFloatVariables() {
    return this.floatVars;
  }

  public Set<String> getDoubleVariables() {
    return this.doubleVars;
  }

  public Set<String> getBooleanVariables() {
    return this.booleanVars;
  }

  public Set<String> getStringVariables() {
    return this.stringVars;
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