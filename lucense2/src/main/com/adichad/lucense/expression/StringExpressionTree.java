package com.adichad.lucense.expression;

import java.util.Set;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.fieldSource.FieldType;
import com.adichad.lucense.expression.node.strings.StringExpressionNode;

public class StringExpressionTree extends ExpressionTree {

  public StringExpressionTree(StringExpressionNode root, Set<String> intVars, Set<String> floatVars,
      Set<String> doubleVars, Set<String> booleanVars, Set<String> stringVars) {
    super(root, intVars, floatVars, doubleVars, booleanVars, stringVars);
  }

  public String evaluate(Slates slates, Context cx) {
    return ((StringExpressionNode) this.root).evaluate(slates, cx);
  }

  public String evaluateFinal(Slates slates, Context cx) {
    return ((StringExpressionNode) this.root).evaluateFinal(slates, cx);
  }

  protected StringExpressionTree(StringExpressionTree tree) {
    super(tree);
  }

  @Override
  public StringExpressionTree clone() {
    return new StringExpressionTree(this);
  }

  @Override
  public final FieldType getType() {
    return FieldType.TYPE_STRING;
  }

}
