package com.adichad.lucense.expression.node.booleans;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.JSExpression;
import com.adichad.lucense.expression.node.doubles.DoubleExpressionNode;
import com.adichad.lucense.expression.node.floats.FloatExpressionNode;
import com.adichad.lucense.expression.node.ints.IntExpressionNode;
import com.adichad.lucense.expression.node.strings.StringExpressionNode;
import com.adichad.lucense.expression.node.strings.StringLiteral;

final public class BooleanJSExpression extends JSExpression implements BooleanExpressionNode {

  public BooleanJSExpression(StringLiteral exprNode, Scriptable scope,
      Map<String, IntExpressionNode> intvars, Map<String, FloatExpressionNode> floatvars,
      Map<String, DoubleExpressionNode> doublevars, Map<String, BooleanExpressionNode> booleanvars,
      Map<String, StringExpressionNode> stringvars) {
    super(exprNode, scope, intvars, floatvars, doublevars, booleanvars, stringvars);
  }

  @Override
  final public boolean evaluate(Slates slates, Context cx) {
    setupScope(slates, cx);
    return Context.toBoolean(this.script.exec(cx, this.scope));
  }

  @Override
  final public boolean evaluateFinal(Slates slates, Context cx) {
    setupScopeFinal(slates, cx);
    return Context.toBoolean(this.script.exec(cx, this.scope));
  }
}
