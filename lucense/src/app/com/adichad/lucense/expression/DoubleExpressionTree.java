package com.adichad.lucense.expression;

import java.util.Set;

import com.adichad.lucense.expression.node.doubles.DoubleExpressionNode;

public class DoubleExpressionTree extends ExpressionTree {

  public DoubleExpressionTree(DoubleExpressionNode root, Set<String> intVars, Set<String> floatVars,
      Set<String> doubleVars, Set<String> booleanVars, Set<String> stringVars) {
    super(root, intVars, floatVars, doubleVars, booleanVars, stringVars);
  }

  public double evaluate() {
    return ((DoubleExpressionNode) this.root).evaluate(this.context, this.slateObject, this.slateInt, this.slateFloat,
        this.slateDouble, this.slateBoolean);
  }

  public double evaluateFinal() {
    return ((DoubleExpressionNode) this.root).evaluateFinal(this.slateObject, this.slateInt, this.slateFloat,
        this.slateDouble, this.slateBoolean);
  }

  protected DoubleExpressionTree(DoubleExpressionTree tree) {
    super(tree);
  }

  @Override
  public DoubleExpressionTree clone() {
    return new DoubleExpressionTree(this);
  }

}
