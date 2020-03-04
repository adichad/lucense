package com.adichad.lucense.expression.node;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.booleans.BooleanExpressionNode;
import com.adichad.lucense.expression.node.doubles.DoubleExpressionNode;
import com.adichad.lucense.expression.node.floats.FloatExpressionNode;
import com.adichad.lucense.expression.node.ints.IntExpressionNode;
import com.adichad.lucense.expression.node.strings.StringExpressionNode;
import com.adichad.lucense.expression.node.strings.StringLiteral;

public abstract class JSExpressionLate implements AggregatorExpressionNode {

  protected final Script                       script;

  private final Scriptable                           scope;

  protected final Map<String, IntExpressionNode>     intvars;

  protected final Map<String, FloatExpressionNode>   floatvars;

  protected final Map<String, DoubleExpressionNode>  doublevars;

  protected final Map<String, BooleanExpressionNode> booleanvars;

  protected final Map<String, StringExpressionNode>  stringvars;

  public JSExpressionLate(StringLiteral exprNode, Scriptable scope,
      Map<String, IntExpressionNode> intvars,
      Map<String, FloatExpressionNode> floatvars,
      Map<String, DoubleExpressionNode> doublevars,
      Map<String, BooleanExpressionNode> booleanvars,
      Map<String, StringExpressionNode> stringvars) {

    String expr = exprNode.evaluate(null, null);

    this.intvars = intvars;
    this.floatvars = floatvars;
    this.doublevars = doublevars;
    this.stringvars = stringvars;
    this.booleanvars = booleanvars;

    this.scope = scope;
    Context cx = Context.enter();
    try {
      this.script = cx.compileString(expr, "<cmd>", 1, null);
    } finally {
      Context.exit();
    }
  }

  @Override
  final public void initSlate(Slates slates, Context cx) {
    Scriptable scope;
    if (this.scope == null) {
      scope = cx.initStandardObjects();
    } else {
      scope = cx.newObject(this.scope);
      scope.setPrototype(this.scope);
      scope.setParentScope(null);
    }
    slates.slateObject.put(this, scope);
  }

  @Override
  final public void updateState(Slates slates, Context cx) {
    setupScope(slates, cx);
  }

  final protected void setupScope(Slates slates, Context cx) {
    Scriptable scope = (Scriptable) slates.slateObject.get(this);
    for (String name : this.intvars.keySet()) {
      ScriptableObject.putProperty(
          scope,
          name,
          Context.javaToJS(
              this.intvars.get(name).evaluate(slates, cx), scope));
    }
    for (String name : this.floatvars.keySet()) {
      ScriptableObject.putProperty(
          scope,
          name,
          Context.javaToJS(
              this.floatvars.get(name).evaluate(slates, cx), scope));
    }
    for (String name : this.doublevars.keySet()) {
      ScriptableObject.putProperty(
          scope,
          name,
          Context.javaToJS(
              this.doublevars.get(name).evaluate(slates, cx), scope));
    }
    for (String name : this.booleanvars.keySet()) {
      ScriptableObject.putProperty(
          scope,
          name,
          Context.javaToJS(
              this.booleanvars.get(name).evaluate(slates, cx), scope));
    }
    for (String name : this.stringvars.keySet()) {
      ScriptableObject.putProperty(
          scope,
          name,
          Context.javaToJS(
              this.stringvars.get(name).evaluate(slates, cx), scope));
    }

  }

  final protected void setupScopeFinal(Slates slates, Context cx) {
    Scriptable scope = (Scriptable) slates.slateObject.get(this);
    for (String name : this.intvars.keySet()) {
      ScriptableObject.putProperty(
          scope,
          name,
          Context.javaToJS(
              this.intvars.get(name).evaluateFinal(slates, cx), scope));
    }
    for (String name : this.floatvars.keySet()) {
      ScriptableObject.putProperty(
          scope,
          name,
          Context.javaToJS(
              this.floatvars.get(name).evaluateFinal(slates, cx), scope));
    }
    for (String name : this.doublevars.keySet()) {
      ScriptableObject.putProperty(
          scope,
          name,
          Context.javaToJS(
              this.doublevars.get(name).evaluateFinal(slates, cx), scope));
    }
    for (String name : this.booleanvars.keySet()) {
      ScriptableObject.putProperty(
          scope,
          name,
          Context.javaToJS(
              this.booleanvars.get(name).evaluateFinal(slates, cx), scope));
    }
    for (String name : this.stringvars.keySet()) {
      ScriptableObject.putProperty(
          scope,
          name,
          Context.javaToJS(
              this.stringvars.get(name).evaluateFinal(slates, cx), scope));
    }

  }

}
