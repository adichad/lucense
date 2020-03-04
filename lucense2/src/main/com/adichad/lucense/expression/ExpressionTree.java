package com.adichad.lucense.expression;

import java.util.Set;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.fieldSource.FieldType;
import com.adichad.lucense.expression.node.ExpressionNode;

public abstract class ExpressionTree implements Cloneable {
  protected final ExpressionNode root;

  protected final Set<String>    intVars;

  protected final Set<String>    floatVars;

  protected final Set<String>    doubleVars;

  protected final Set<String>    booleanVars;

  protected final Set<String>    stringVars;

  public ExpressionTree(ExpressionNode root, Set<String> intVars,
      Set<String> floatVars, Set<String> doubleVars, Set<String> booleanVars,
      Set<String> stringVars) {
    this.root = root;
    this.intVars = intVars;
    this.floatVars = floatVars;
    this.doubleVars = doubleVars;
    this.booleanVars = booleanVars;
    this.stringVars = stringVars;
  }

  protected ExpressionTree(ExpressionTree tree) {
    this.root = tree.root;
    this.intVars = tree.intVars;
    this.floatVars = tree.floatVars;
    this.doubleVars = tree.doubleVars;
    this.booleanVars = tree.booleanVars;
    this.stringVars = tree.stringVars;
  }

  public Slates initState(Context cx) {
    return new Slates(0);
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
  
  public abstract FieldType getType();

}
