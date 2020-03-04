package com.adichad.lucense.expression;

import java.util.List;
import java.util.Set;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.node.AggregatorExpressionNode;
import com.adichad.lucense.expression.node.doubles.DoubleExpressionNode;

public class DoubleAggregatingExpressionTree extends DoubleExpressionTree {
  protected List<AggregatorExpressionNode> aggregateNodes;

  public DoubleAggregatingExpressionTree(DoubleExpressionNode root, Set<String> intVars, Set<String> floatVars,
      Set<String> doubleVars, Set<String> booleanVars, Set<String> stringVars,
      List<AggregatorExpressionNode> aggregateNodes) {
    super(root, intVars, floatVars, doubleVars, booleanVars, stringVars);
    this.aggregateNodes = aggregateNodes;
  }
  
  @Override
  public Slates initState(Context cx) {
    Slates slates = new Slates();
    for (AggregatorExpressionNode aggregator : aggregateNodes) {
      aggregator.initSlate(slates, cx);
    }
    return slates;
  }

  @Override
  public double evaluate(Slates slates, Context cx) {
    for (AggregatorExpressionNode node : this.aggregateNodes) {
      node.updateState(slates, cx);
    }
    return 0d;
  }

  protected DoubleAggregatingExpressionTree(DoubleAggregatingExpressionTree tree) {
    super(tree);
    this.aggregateNodes = tree.aggregateNodes;
  }

  @Override
  public DoubleAggregatingExpressionTree clone() {
    return new DoubleAggregatingExpressionTree(this);
  }

}
