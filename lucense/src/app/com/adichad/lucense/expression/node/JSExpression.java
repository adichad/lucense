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

public abstract class JSExpression implements ExpressionNode {

  protected Script script;

  protected final Scriptable scope; // FIXME:
                                    // thread-safety
                                    // issue!

  protected Context cx;

  protected Map<String, IntExpressionNode> intvars;

  protected Map<String, FloatExpressionNode> floatvars;

  protected Map<String, DoubleExpressionNode> doublevars;

  protected Map<String, BooleanExpressionNode> booleanvars;

  protected Map<String, StringExpressionNode> stringvars;

  public JSExpression(StringLiteral exprNode, Context cx, Scriptable scope, Map<String, IntExpressionNode> intvars,
      Map<String, FloatExpressionNode> floatvars, Map<String, DoubleExpressionNode> doublevars,
      Map<String, BooleanExpressionNode> booleanvars, Map<String, StringExpressionNode> stringvars) {

    String expr = exprNode.evaluate(null, null, null, null, null, null);
    this.cx = cx;

    this.intvars = intvars;
    this.floatvars = floatvars;
    this.doublevars = doublevars;
    this.stringvars = stringvars;
    this.booleanvars = booleanvars;

    if (scope == null) {
      this.scope = cx.initStandardObjects();
    } else {
      this.scope = cx.newObject(scope);
      this.scope.setPrototype(scope);
      this.scope.setParentScope(null);
    }
    this.script = cx.compileString(expr, "<cmd>", 1, null);
  }

  protected void setupScope(VarContext context, Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {

    for (String name : this.intvars.keySet()) {
      ScriptableObject.putProperty(this.scope, name, Context.javaToJS(
          this.intvars.get(name).evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean),
          this.scope));
    }
    for (String name : this.floatvars.keySet()) {
      ScriptableObject.putProperty(this.scope, name, Context.javaToJS(
          this.floatvars.get(name).evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean),
          this.scope));
    }
    for (String name : this.doublevars.keySet()) {
      ScriptableObject.putProperty(this.scope, name, Context.javaToJS(
          this.doublevars.get(name).evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean),
          this.scope));
    }
    for (String name : this.booleanvars.keySet()) {
      ScriptableObject.putProperty(this.scope, name, Context.javaToJS(
          this.booleanvars.get(name).evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean),
          this.scope));
    }
    for (String name : this.stringvars.keySet()) {
      ScriptableObject.putProperty(this.scope, name, Context.javaToJS(
          this.stringvars.get(name).evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean),
          this.scope));
    }

  }

  protected void setupScopeFinal(Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    for (String name : this.intvars.keySet()) {
      ScriptableObject.putProperty(this.scope, name, Context.javaToJS(
          this.intvars.get(name).evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean),
          this.scope));
    }
    for (String name : this.floatvars.keySet()) {
      ScriptableObject.putProperty(this.scope, name, Context.javaToJS(
          this.floatvars.get(name).evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean),
          this.scope));
    }
    for (String name : this.doublevars.keySet()) {
      ScriptableObject.putProperty(this.scope, name, Context.javaToJS(
          this.doublevars.get(name).evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean),
          this.scope));
    }
    for (String name : this.booleanvars.keySet()) {
      ScriptableObject.putProperty(this.scope, name, Context.javaToJS(
          this.booleanvars.get(name).evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean),
          this.scope));
    }
    for (String name : this.stringvars.keySet()) {
      ScriptableObject.putProperty(this.scope, name, Context.javaToJS(
          this.stringvars.get(name).evaluateFinal(slateObject, slateInt, slateFloat, slateDouble, slateBoolean),
          this.scope));
    }

  }

}
