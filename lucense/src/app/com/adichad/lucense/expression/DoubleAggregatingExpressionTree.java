package com.adichad.lucense.expression;

import java.util.List;
import java.util.Set;

import com.adichad.lucense.expression.node.AggregatorExpressionNode;
import com.adichad.lucense.expression.node.doubles.DoubleExpressionNode;
import com.adichad.lucense.expression.node.ints.IntExpressionNode;

public class DoubleAggregatingExpressionTree extends DoubleExpressionTree {
  protected List<AggregatorExpressionNode> aggregateNodes;

  public DoubleAggregatingExpressionTree(DoubleExpressionNode root, Set<String> intVars, Set<String> floatVars,
      Set<String> doubleVars, Set<String> booleanVars, Set<String> stringVars,
      List<AggregatorExpressionNode> aggregateNodes) {
    super(root, intVars, floatVars, doubleVars, booleanVars, stringVars);
    this.aggregateNodes = aggregateNodes;

    for (AggregatorExpressionNode key : aggregateNodes) {
      key.initSlate(this.slateObject, this.slateInt, this.slateFloat, this.slateDouble, this.slateBoolean);
    }
  }

  @Override
  public double evaluate() {
    for (AggregatorExpressionNode node : this.aggregateNodes) {
      node.updateState(this.context, this.slateObject, this.slateInt, this.slateFloat, this.slateDouble,
          this.slateBoolean);
    }
    return 0d;
  }

  @Override
  public double evaluateFinal() {
    return ((IntExpressionNode) this.root).evaluateFinal(this.slateObject, this.slateInt, this.slateFloat,
        this.slateDouble, this.slateBoolean);
  }

  protected DoubleAggregatingExpressionTree(DoubleAggregatingExpressionTree tree) {
    super(tree);
    this.aggregateNodes = tree.aggregateNodes;

    for (AggregatorExpressionNode key : this.aggregateNodes) {
      key.initSlate(this.slateObject, this.slateInt, this.slateFloat, this.slateDouble, this.slateBoolean);
    }
  }

  @Override
  public DoubleAggregatingExpressionTree clone() {
    return new DoubleAggregatingExpressionTree(this);
  }

}
