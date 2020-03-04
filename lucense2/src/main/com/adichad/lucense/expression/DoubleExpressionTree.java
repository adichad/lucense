package com.adichad.lucense.expression;

import java.util.Set;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.fieldSource.FieldType;
import com.adichad.lucense.expression.node.doubles.DoubleExpressionNode;

public class DoubleExpressionTree extends ExpressionTree {

  public DoubleExpressionTree(DoubleExpressionNode root, Set<String> intVars, Set<String> floatVars,
      Set<String> doubleVars, Set<String> booleanVars, Set<String> stringVars) {
    super(root, intVars, floatVars, doubleVars, booleanVars, stringVars);
  }

  public double evaluate(Slates slates, Context cx) {
    return ((DoubleExpressionNode) this.root).evaluate(slates, cx);
  }

  public double evaluateFinal(Slates slates, Context cx) {
    return ((DoubleExpressionNode) this.root).evaluateFinal(slates, cx);
  }

  protected DoubleExpressionTree(DoubleExpressionTree tree) {
    super(tree);
  }

  @Override
  public DoubleExpressionTree clone() {
    return new DoubleExpressionTree(this);
  }

  @Override
  public final FieldType getType() {
    return FieldType.TYPE_DOUBLE;
  }

}
