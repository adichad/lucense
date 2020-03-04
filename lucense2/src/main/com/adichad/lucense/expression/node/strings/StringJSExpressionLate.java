package com.adichad.lucense.expression.node.strings;

import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.JSExpressionLate;
import com.adichad.lucense.expression.node.booleans.BooleanExpressionNode;
import com.adichad.lucense.expression.node.doubles.DoubleExpressionNode;
import com.adichad.lucense.expression.node.floats.FloatExpressionNode;
import com.adichad.lucense.expression.node.ints.IntExpressionNode;

public class StringJSExpressionLate extends JSExpressionLate implements
    StringExpressionNode {

  public StringJSExpressionLate(StringLiteral exprNode, Scriptable scope,
      Map<String, IntExpressionNode> intvars,
      Map<String, FloatExpressionNode> floatvars,
      Map<String, DoubleExpressionNode> doublevars,
      Map<String, BooleanExpressionNode> booleanvars,
      Map<String, StringExpressionNode> stringvars) {
    super(exprNode, scope, intvars, floatvars, doublevars, booleanvars,
        stringvars);
  }

  @Override
  public String evaluate(Slates slates, Context cx) {
    return null;
  }

  @Override
  public String evaluateFinal(Slates slates, Context cx) {
    setupScopeFinal(slates, cx);
    return Context.toString(this.script.exec(cx,
        (Scriptable) slates.slateObject.get(this)));
  }

}
