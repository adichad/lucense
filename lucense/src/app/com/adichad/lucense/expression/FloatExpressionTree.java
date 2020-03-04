package com.adichad.lucense.expression;

import java.util.Set;

import com.adichad.lucense.expression.node.floats.FloatExpressionNode;

public class FloatExpressionTree extends ExpressionTree {

  public FloatExpressionTree(FloatExpressionNode root, Set<String> intVars, Set<String> floatVars,
      Set<String> doubleVars, Set<String> booleanVars, Set<String> stringVars) {
    super(root, intVars, floatVars, doubleVars, booleanVars, stringVars);
  }

  public float evaluate() {
    return ((FloatExpressionNode) this.root).evaluate(this.context, this.slateObject, this.slateInt, this.slateFloat,
        this.slateDouble, this.slateBoolean);
  }

  public float evaluateFinal() {
    return ((FloatExpressionNode) this.root).evaluateFinal(this.slateObject, this.slateInt, this.slateFloat,
        this.slateDouble, this.slateBoolean);
  }

  protected FloatExpressionTree(FloatExpressionTree tree) {
    super(tree);
  }

  @Override
  public FloatExpressionTree clone() {
    return new FloatExpressionTree(this);
  }

}
