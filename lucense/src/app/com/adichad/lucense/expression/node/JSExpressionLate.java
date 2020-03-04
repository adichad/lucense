package com.adichad.lucense.expression.node;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.adichad.lucense.expression.VarContext;
import com.adichad.lucense.expression.node.booleans.BooleanExpressionNode;
import com.adichad.lucense.expression.node.doubles.DoubleExpressionNode;
import com.adichad.lucense.expression.node.floats.FloatExpressionNode;
import com.adichad.lucense.expression.node.ints.IntExpressionNode;
import com.adichad.lucense.expression.node.strings.StringExpressionNode;
import com.adichad.lucense.expression.node.strings.StringLiteral;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public abstract class JSExpressionLate implements AggregatorExpressionNode {

  protected final Script script;

  protected Context cx;

  private Scriptable scope;

  protected Map<String, IntExpressionNode> intvars;

  protected Map<String, FloatExpressionNode> floatvars;

  protected Map<String, DoubleExpressionNode> doublevars;

  protected Map<String, BooleanExpressionNode> booleanvars;

  protected Map<String, StringExpressionNode> stringvars;

  public JSExpressionLate(StringLiteral exprNode, Context cx, Scriptable scope, Map<String, IntExpressionNode> intvars,
      Map<String, FloatExpressionNode> floatvars, Map<String, DoubleExpressionNode> doublevars,
      Map<String, BooleanExpressionNode> booleanvars, Map<String, StringExpressionNode> stringvars) {

    String expr = exprNode.evaluate(null, null, null, null, null, null);
    this.cx = cx;

    this.intvars = intvars;
    this.floatvars = floatvars;
    this.doublevars = doublevars;
    this.stringvars = stringvars;
    this.booleanvars = booleanvars;

    this.scope = scope;
    this.script = cx.compileString(expr, "<cmd>", 1, null);
  }

  @Override
  public void reset() {}

  @Override
  public void initSlate(Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    Scriptable scope;
    if (this.scope == null) {
      scope = this.cx.initStandardObjects();
    } else {
      scope = this.cx.newObject(this.scope);
      scope.setPrototype(this.scope);
      scope.setParentScope(null);
    }
    slateObject.put(this, scope);
  }

  @Override
  public void updateState(VarContext context, Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    setupScope(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
  }

  protected void setupScope(VarContext context, Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    Scriptable scope = (Scriptable) slateObject.get(this);
    for (String name : this.intvars.keySet()) {
      ScriptableObject.putProperty(scope, name, Context
          .javaToJS(
              this.intvars.get(name).evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean),
              scope));
    }
    for (String name : this.floatvars.keySet()) {
      ScriptableObject.putProperty(scope, name, Context.javaToJS(
          this.floatvars.get(name).evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean),
          scope));
    }
    for (String name : this.doublevars.keySet()) {
      ScriptableObject.putProperty(scope, name, Context.javaToJS(
          this.doublevars.get(name).evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean),
          scope));
    }
    for (String name : this.booleanvars.keySet()) {
      ScriptableObject.putProperty(scope, name, Context.javaToJS(
          this.booleanvars.get(name).evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean),
          scope));
    }
    for (String name : this.stringvars.keySet()) {
      ScriptableObject.putProperty(scope, name, Context.javaToJS(
          this.stringvars.get(name).evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean),
          scope));
    }

  }

  protected void setupScopeFinal(Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    Scriptable scope = (Scriptable) slateObject.get(this);
    for (String name : this.intvars.keySet()) {
      ScriptableObject.putProperty(scope, name, Context.javaToJS(
          this.intvars.get(name).evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean), scope));
    }
    for (String name : this.floatvars.keySet()) {
      ScriptableObject.putProperty(scope, name, Context.javaToJS(
          this.floatvars.get(name).evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean), scope));
    }
    for (String name : this.doublevars.keySet()) {
      ScriptableObject
          .putProperty(scope, name, Context.javaToJS(
              this.doublevars.get(name).evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean),
              scope));
    }
    for (String name : this.booleanvars.keySet()) {
      ScriptableObject.putProperty(scope, name, Context
          .javaToJS(
              this.booleanvars.get(name).evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean),
              scope));
    }
    for (String name : this.stringvars.keySet()) {
      ScriptableObject
          .putProperty(scope, name, Context.javaToJS(
              this.stringvars.get(name).evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean),
              scope));
    }

  }

}
