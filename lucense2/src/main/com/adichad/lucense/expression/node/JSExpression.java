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

public abstract class JSExpression implements ExpressionNode {

  protected final Script                             script;

  protected final Scriptable                   scope;      // FIXME:
                                                            // thread-safety
                                                            // issue!

  protected final Map<String, IntExpressionNode>     intvars;

  protected final Map<String, FloatExpressionNode>   floatvars;

  protected final Map<String, DoubleExpressionNode>  doublevars;

  protected final Map<String, BooleanExpressionNode> booleanvars;

  protected final Map<String, StringExpressionNode>  stringvars;

  public JSExpression(StringLiteral exprNode, Scriptable scope,
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

    Context cx = Context.enter();
    try {
      if (scope == null) {
        this.scope = cx.initStandardObjects();
      } else {
        this.scope = cx.newObject(scope);
        this.scope.setPrototype(scope);
        this.scope.setParentScope(null);
      }
      this.script = cx.compileString(expr, "<cmd>", 1, null);
    } finally {
      Context.exit();
    }
  }

  final protected void setupScope(Slates slates, Context cx) {

    for (String name : this.intvars.keySet()) {
      ScriptableObject.putProperty(this.scope, name, Context.javaToJS(
          this.intvars.get(name).evaluate(slates, cx), this.scope));
    }
    for (String name : this.floatvars.keySet()) {
      ScriptableObject.putProperty(this.scope, name, Context.javaToJS(
          this.floatvars.get(name).evaluate(slates, cx), this.scope));
    }
    for (String name : this.doublevars.keySet()) {
      ScriptableObject.putProperty(this.scope, name, Context.javaToJS(
          this.doublevars.get(name).evaluate(slates, cx), this.scope));
    }
    for (String name : this.booleanvars.keySet()) {
      ScriptableObject.putProperty(this.scope, name, Context.javaToJS(
          this.booleanvars.get(name).evaluate(slates, cx), this.scope));
    }
    for (String name : this.stringvars.keySet()) {
      ScriptableObject.putProperty(this.scope, name, Context.javaToJS(
          this.stringvars.get(name).evaluate(slates, cx), this.scope));
    }

  }

  final protected void setupScopeFinal(Slates slates, Context cx) {
    for (String name : this.intvars.keySet()) {
      ScriptableObject.putProperty(this.scope, name, Context.javaToJS(
          this.intvars.get(name).evaluateFinal(slates, cx), this.scope));
    }
    for (String name : this.floatvars.keySet()) {
      ScriptableObject.putProperty(this.scope, name, Context.javaToJS(
          this.floatvars.get(name).evaluateFinal(slates, cx), this.scope));
    }
    for (String name : this.doublevars.keySet()) {
      ScriptableObject.putProperty(this.scope, name, Context.javaToJS(
          this.doublevars.get(name).evaluateFinal(slates, cx), this.scope));
    }
    for (String name : this.booleanvars.keySet()) {
      ScriptableObject.putProperty(this.scope, name, Context.javaToJS(
          this.booleanvars.get(name).evaluateFinal(slates, cx), this.scope));
    }
    for (String name : this.stringvars.keySet()) {
      ScriptableObject.putProperty(this.scope, name, Context.javaToJS(
          this.stringvars.get(name).evaluateFinal(slates, cx), this.scope));
    }

  }

}
