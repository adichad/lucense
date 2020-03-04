package com.adichad.lucense.expression;

import java.util.Set;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.fieldSource.FieldType;
import com.adichad.lucense.expression.node.floats.FloatExpressionNode;

public class FloatExpressionTree extends ExpressionTree {

  public FloatExpressionTree(FloatExpressionNode root, Set<String> intVars, Set<String> floatVars,
      Set<String> doubleVars, Set<String> booleanVars, Set<String> stringVars) {
    super(root, intVars, floatVars, doubleVars, booleanVars, stringVars);
  }

  public float evaluate(Slates slates, Context cx) {
    return ((FloatExpressionNode) this.root).evaluate(slates, cx);
  }

  public float evaluateFinal(Slates slates, Context cx) {
    return ((FloatExpressionNode) this.root).evaluateFinal(slates, cx);
  }

  protected FloatExpressionTree(FloatExpressionTree tree) {
    super(tree);
  }

  @Override
  public FloatExpressionTree clone() {
    return new FloatExpressionTree(this);
  }

  @Override
  public final FieldType getType() {
    return FieldType.TYPE_FLOAT;
  }

}
