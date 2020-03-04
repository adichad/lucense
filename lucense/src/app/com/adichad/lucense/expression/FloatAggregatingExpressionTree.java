package com.adichad.lucense.expression;

import java.util.List;
import java.util.Set;

import com.adichad.lucense.expression.node.AggregatorExpressionNode;
import com.adichad.lucense.expression.node.floats.FloatExpressionNode;

public class FloatAggregatingExpressionTree extends FloatExpressionTree {
  protected List<AggregatorExpressionNode> aggregateNodes;

  public FloatAggregatingExpressionTree(FloatExpressionNode root, Set<String> intVars, Set<String> floatVars,
      Set<String> doubleVars, Set<String> booleanVars, Set<String> stringVars,
      List<AggregatorExpressionNode> aggregateNodes) {
    super(root, intVars, floatVars, doubleVars, booleanVars, stringVars);
    this.aggregateNodes = aggregateNodes;

    for (AggregatorExpressionNode key : aggregateNodes) {
      key.initSlate(this.slateObject, this.slateInt, this.slateFloat, this.slateDouble, this.slateBoolean);
    }
  }

  @Override
  public float evaluate() {
    for (AggregatorExpressionNode node : this.aggregateNodes) {
      node.updateState(this.context, this.slateObject, this.slateInt, this.slateFloat, this.slateDouble,
          this.slateBoolean);
    }
    return 0f;
  }

  @Override
  public float evaluateFinal() {
    return ((FloatExpressionNode) this.root).evaluateFinal(this.slateObject, this.slateInt, this.slateFloat,
        this.slateDouble, this.slateBoolean);
  }

  protected FloatAggregatingExpressionTree(FloatAggregatingExpressionTree tree) {
    super(tree);
    this.aggregateNodes = tree.aggregateNodes;

    for (AggregatorExpressionNode key : this.aggregateNodes) {
      key.initSlate(this.slateObject, this.slateInt, this.slateFloat, this.slateDouble, this.slateBoolean);
    }
  }

  @Override
  public FloatAggregatingExpressionTree clone() {
    return new FloatAggregatingExpressionTree(this);
  }

}
