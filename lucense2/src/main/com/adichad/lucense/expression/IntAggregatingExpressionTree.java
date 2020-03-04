package com.adichad.lucense.expression;

import java.util.List;
import java.util.Set;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.node.AggregatorExpressionNode;
import com.adichad.lucense.expression.node.ints.IntExpressionNode;

public class IntAggregatingExpressionTree extends IntExpressionTree {
  protected List<AggregatorExpressionNode> aggregateNodes;

  public IntAggregatingExpressionTree(IntExpressionNode root, Set<String> intVars, Set<String> floatVars,
      Set<String> doubleVars, Set<String> booleanVars, Set<String> stringVars,
      List<AggregatorExpressionNode> aggregateNodes) {
    super(root, intVars, floatVars, doubleVars, booleanVars, stringVars);
    this.aggregateNodes = aggregateNodes;
  }

  @Override
  public Slates initState(Context cx) {
    Slates slates = new Slates();
    for (AggregatorExpressionNode key : aggregateNodes) {
      key.initSlate(slates, cx);
    }
    return slates;
  }

  @Override
  public int evaluate(Slates slates, Context cx) {
    for (AggregatorExpressionNode node : this.aggregateNodes) {
      node.updateState(slates, cx);
    }
    return 0;
  }

  protected IntAggregatingExpressionTree(IntAggregatingExpressionTree tree) {
    super(tree);
    this.aggregateNodes = tree.aggregateNodes;
  }

  @Override
  public IntAggregatingExpressionTree clone() {
    return new IntAggregatingExpressionTree(this);
  }

}
