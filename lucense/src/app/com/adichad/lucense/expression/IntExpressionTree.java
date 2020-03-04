package com.adichad.lucense.expression;

import java.util.Set;

import com.adichad.lucense.expression.node.ints.IntExpressionNode;

public class IntExpressionTree extends ExpressionTree {

  public IntExpressionTree(IntExpressionNode root, Set<String> intVars, Set<String> floatVars, Set<String> doubleVars,
      Set<String> booleanVars, Set<String> stringVars) {
    super(root, intVars, floatVars, doubleVars, booleanVars, stringVars);
  }

  public int evaluate() {
    return ((IntExpressionNode) this.root).evaluate(this.context, this.slateObject, this.slateInt, this.slateFloat,
        this.slateDouble, this.slateBoolean);
  }

  public int evaluateFinal() {
    return ((IntExpressionNode) this.root).evaluateFinal(this.slateObject, this.slateInt, this.slateFloat,
        this.slateDouble, this.slateBoolean);
  }

  protected IntExpressionTree(IntExpressionTree tree) {
    super(tree);
  }

  @Override
  public IntExpressionTree clone() {
    return new IntExpressionTree(this);
  }

}
