package com.adichad.lucense.expression;

import java.util.Set;

import com.adichad.lucense.expression.node.strings.StringExpressionNode;

public class StringExpressionTree extends ExpressionTree {

  public StringExpressionTree(StringExpressionNode root, Set<String> intVars, Set<String> floatVars,
      Set<String> doubleVars, Set<String> booleanVars, Set<String> stringVars) {
    super(root, intVars, floatVars, doubleVars, booleanVars, stringVars);
  }

  public String evaluate() {
    return ((StringExpressionNode) this.root).evaluate(this.context, this.slateObject, this.slateInt, this.slateFloat,
        this.slateDouble, this.slateBoolean);
  }

  public String evaluateFinal() {
    return ((StringExpressionNode) this.root).evaluateFinal(this.slateObject, this.slateInt, this.slateFloat,
        this.slateDouble, this.slateBoolean);
  }

  protected StringExpressionTree(StringExpressionTree tree) {
    super(tree);
  }

  @Override
  public StringExpressionTree clone() {
    return new StringExpressionTree(this);
  }

}
