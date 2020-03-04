package com.adichad.lucense.expression;

import java.util.List;
import java.util.Set;

import com.adichad.lucense.expression.node.AggregatorExpressionNode;
import com.adichad.lucense.expression.node.ints.IntExpressionNode;

public class IntAggregatingExpressionTree extends IntExpressionTree {
  protected List<AggregatorExpressionNode> aggregateNodes;

  public IntAggregatingExpressionTree(IntExpressionNode root, Set<String> intVars, Set<String> floatVars,
      Set<String> doubleVars, Set<String> booleanVars, Set<String> stringVars,
      List<AggregatorExpressionNode> aggregateNodes) {
    super(root, intVars, floatVars, doubleVars, booleanVars, stringVars);
    this.aggregateNodes = aggregateNodes;

    for (AggregatorExpressionNode key : aggregateNodes) {
      key.initSlate(this.slateObject, this.slateInt, this.slateFloat, this.slateDouble, this.slateBoolean);
    }
  }

  @Override
  public int evaluate() {
    for (AggregatorExpressionNode node : this.aggregateNodes) {
      node.updateState(this.context, this.slateObject, this.slateInt, this.slateFloat, this.slateDouble,
          this.slateBoolean);
    }
    return 0;
  }

  @Override
  public int evaluateFinal() {
    return ((IntExpressionNode) this.root).evaluateFinal(this.slateObject, this.slateInt, this.slateFloat,
        this.slateDouble, this.slateBoolean);
  }

  protected IntAggregatingExpressionTree(IntAggregatingExpressionTree tree) {
    super(tree);
    this.aggregateNodes = tree.aggregateNodes;

    for (AggregatorExpressionNode key : this.aggregateNodes) {
      key.initSlate(this.slateObject, this.slateInt, this.slateFloat, this.slateDouble, this.slateBoolean);
    }
  }

  @Override
  public IntAggregatingExpressionTree clone() {
    return new IntAggregatingExpressionTree(this);
  }

}
