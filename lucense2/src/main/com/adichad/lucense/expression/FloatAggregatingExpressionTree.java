package com.adichad.lucense.expression;

import java.util.List;
import java.util.Set;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.node.AggregatorExpressionNode;
import com.adichad.lucense.expression.node.floats.FloatExpressionNode;

public class FloatAggregatingExpressionTree extends FloatExpressionTree {
  protected List<AggregatorExpressionNode> aggregateNodes;

  public FloatAggregatingExpressionTree(FloatExpressionNode root, Set<String> intVars, Set<String> floatVars,
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
  public float evaluate(Slates slates, Context cx) {
    for (AggregatorExpressionNode node : this.aggregateNodes) {
      node.updateState(slates, cx);
    }
    return 0f;
  }

  protected FloatAggregatingExpressionTree(FloatAggregatingExpressionTree tree) {
    super(tree);
    this.aggregateNodes = tree.aggregateNodes;
  }

  @Override
  public FloatAggregatingExpressionTree clone() {
    return new FloatAggregatingExpressionTree(this);
  }

}
