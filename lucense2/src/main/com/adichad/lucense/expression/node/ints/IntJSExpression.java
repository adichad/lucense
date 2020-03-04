package com.adichad.lucense.expression.node.ints;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.JSExpression;
import com.adichad.lucense.expression.node.booleans.BooleanExpressionNode;
import com.adichad.lucense.expression.node.doubles.DoubleExpressionNode;
import com.adichad.lucense.expression.node.floats.FloatExpressionNode;
import com.adichad.lucense.expression.node.strings.StringExpressionNode;
import com.adichad.lucense.expression.node.strings.StringLiteral;

final public class IntJSExpression extends JSExpression implements IntExpressionNode {
  public IntJSExpression(StringLiteral exprNode, Scriptable scope, Map<String, IntExpressionNode> intvars,
      Map<String, FloatExpressionNode> floatvars, Map<String, DoubleExpressionNode> doublevars,
      Map<String, BooleanExpressionNode> booleanvars, Map<String, StringExpressionNode> stringvars) {
    super(exprNode, scope, intvars, floatvars, doublevars, booleanvars, stringvars);
  }

  @Override
  final public int evaluate(Slates slates, Context cx) {
    setupScope(slates, cx);
    return ((Double) Context.toNumber(this.script.exec(cx, this.scope))).intValue();
  }

  @Override
  final public int evaluateFinal(Slates slates, Context cx) {
    setupScopeFinal(slates, cx);
    return ((Double) Context.toNumber(this.script.exec(cx, this.scope))).intValue();
  }
}
