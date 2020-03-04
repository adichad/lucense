package com.adichad.lucense.expression;

import java.util.Set;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.fieldSource.FieldType;
import com.adichad.lucense.expression.node.booleans.BooleanExpressionNode;

public class BooleanExpressionTree extends ExpressionTree {

  public BooleanExpressionTree(BooleanExpressionNode root, Set<String> intVars, Set<String> floatVars,
      Set<String> doubleVars, Set<String> booleanVars, Set<String> stringVars) {
    super(root, intVars, floatVars, doubleVars, booleanVars, stringVars);
  }

  public boolean evaluate(Slates slates, Context cx) {
    return ((BooleanExpressionNode) this.root).evaluate(slates, cx);
  }

  public boolean evaluateFinal(Slates slates, Context cx) {
    return ((BooleanExpressionNode) this.root).evaluateFinal(slates, cx);
  }

  protected BooleanExpressionTree(BooleanExpressionTree tree) {
    super(tree);
  }

  @Override
  public BooleanExpressionTree clone() {
    return new BooleanExpressionTree(this);
  }

  @Override
  public final FieldType getType() {
    return FieldType.TYPE_BOOLEAN;
  }

}
