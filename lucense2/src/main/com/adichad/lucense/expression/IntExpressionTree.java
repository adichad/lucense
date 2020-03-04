package com.adichad.lucense.expression;

import java.util.Set;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.fieldSource.FieldType;
import com.adichad.lucense.expression.node.ints.IntExpressionNode;

public class IntExpressionTree extends ExpressionTree {

  public IntExpressionTree(IntExpressionNode root, Set<String> intVars, Set<String> floatVars, Set<String> doubleVars,
      Set<String> booleanVars, Set<String> stringVars) {
    super(root, intVars, floatVars, doubleVars, booleanVars, stringVars);
  }

  public int evaluate(Slates slates, Context cx) {
    return ((IntExpressionNode) this.root).evaluate(slates, cx);
  }

  public int evaluateFinal(Slates slates, Context cx) {
    return ((IntExpressionNode) this.root).evaluateFinal(slates, cx);
  }

  protected IntExpressionTree(IntExpressionTree tree) {
    super(tree);
  }

  @Override
  public IntExpressionTree clone() {
    return new IntExpressionTree(this);
  }

  @Override
  public final FieldType getType() {
    return FieldType.TYPE_INT;
  }

}
