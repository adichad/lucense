package com.adichad.lucense.expression;

import java.util.Set;

import com.adichad.lucense.expression.node.booleans.BooleanExpressionNode;

public class BooleanExpressionTree extends ExpressionTree {

  public BooleanExpressionTree(BooleanExpressionNode root, Set<String> intVars, Set<String> floatVars,
      Set<String> doubleVars, Set<String> booleanVars, Set<String> stringVars) {
    super(root, intVars, floatVars, doubleVars, booleanVars, stringVars);
  }

  public boolean evaluate() {
    return ((BooleanExpressionNode) this.root).evaluate(this.context, this.slateObject, this.slateInt, this.slateFloat,
        this.slateDouble, this.slateBoolean);
  }

  public boolean evaluateFinal() {
    return ((BooleanExpressionNode) this.root).evaluateFinal(this.slateObject, this.slateInt, this.slateFloat,
        this.slateDouble, this.slateBoolean);
  }

  protected BooleanExpressionTree(BooleanExpressionTree tree) {
    super(tree);
  }

  @Override
  public BooleanExpressionTree clone() {
    return new BooleanExpressionTree(this);
  }

}
