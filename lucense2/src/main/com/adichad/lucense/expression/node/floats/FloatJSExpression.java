package com.adichad.lucense.expression.node.floats;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.JSExpression;
import com.adichad.lucense.expression.node.booleans.BooleanExpressionNode;
import com.adichad.lucense.expression.node.doubles.DoubleExpressionNode;
import com.adichad.lucense.expression.node.ints.IntExpressionNode;
import com.adichad.lucense.expression.node.strings.StringExpressionNode;
import com.adichad.lucense.expression.node.strings.StringLiteral;

public class FloatJSExpression extends JSExpression implements
    FloatExpressionNode {
  public FloatJSExpression(StringLiteral exprNode, Scriptable scope,
      Map<String, IntExpressionNode> intvars,
      Map<String, FloatExpressionNode> floatvars,
      Map<String, DoubleExpressionNode> doublevars,
      Map<String, BooleanExpressionNode> booleanvars,
      Map<String, StringExpressionNode> stringvars) {
    super(exprNode, scope, intvars, floatvars, doublevars, booleanvars,
        stringvars);
  }

  @Override
  public float evaluate(Slates slates, Context cx) {
    setupScope(slates, cx);
    return ((Double) Context.toNumber(this.script.exec(cx, this.scope)))
        .floatValue();
  }

  @Override
  public float evaluateFinal(Slates slates, Context cx) {
    setupScopeFinal(slates, cx);
    return ((Double) Context.toNumber(this.script.exec(cx, this.scope)))
        .floatValue();
  }
}
