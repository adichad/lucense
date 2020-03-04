package com.adichad.lucense.expression;

import java.util.List;
import java.util.Set;

import com.adichad.lucense.expression.node.AggregatorExpressionNode;
import com.adichad.lucense.expression.node.booleans.BooleanExpressionNode;

public class BooleanAggregatingExpressionTree extends BooleanExpressionTree {
  protected List<AggregatorExpressionNode> aggregateNodes;

  public BooleanAggregatingExpressionTree(BooleanExpressionNode root, Set<String> intVars, Set<String> floatVars,
      Set<String> doubleVars, Set<String> booleanVars, Set<String> stringVars,
      List<AggregatorExpressionNode> aggregateNodes) {
    super(root, intVars, floatVars, doubleVars, booleanVars, stringVars);
    this.aggregateNodes = aggregateNodes;

    for (AggregatorExpressionNode key : aggregateNodes) {
      key.initSlate(this.slateObject, this.slateInt, this.slateFloat, this.slateDouble, this.slateBoolean);
    }
  }

  @Override
  public boolean evaluate() {
    for (AggregatorExpressionNode node : this.aggregateNodes) {
      node.updateState(this.context, this.slateObject, this.slateInt, this.slateFloat, this.slateDouble,
          this.slateBoolean);
    }
    return false;
  }

  @Override
  public boolean evaluateFinal() {
    return ((BooleanExpressionNode) this.root).evaluateFinal(this.slateObject, this.slateInt, this.slateFloat,
        this.slateDouble, this.slateBoolean);
  }

  protected BooleanAggregatingExpressionTree(BooleanAggregatingExpressionTree tree) {
    super(tree);
    this.aggregateNodes = tree.aggregateNodes;

    for (AggregatorExpressionNode key : this.aggregateNodes) {
      key.initSlate(this.slateObject, this.slateInt, this.slateFloat, this.slateDouble, this.slateBoolean);
    }
  }

  @Override
  public BooleanAggregatingExpressionTree clone() {
    return new BooleanAggregatingExpressionTree(this);
  }

}
