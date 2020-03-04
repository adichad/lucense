package com.adichad.lucense.expression;

import java.util.List;
import java.util.Set;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.node.AggregatorExpressionNode;
import com.adichad.lucense.expression.node.strings.StringExpressionNode;

public class StringAggregatingExpressionTree extends StringExpressionTree {
  protected List<AggregatorExpressionNode> aggregateNodes;

  public StringAggregatingExpressionTree(StringExpressionNode root, Set<String> intVars, Set<String> floatVars,
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
  public String evaluate(Slates slates, Context cx) {
    for (AggregatorExpressionNode node : this.aggregateNodes) {
      node.updateState(slates, cx);
    }
    return null;
  }

  protected StringAggregatingExpressionTree(StringAggregatingExpressionTree tree) {
    super(tree);
    this.aggregateNodes = tree.aggregateNodes;
  }

  @Override
  public StringAggregatingExpressionTree clone() {
    return new StringAggregatingExpressionTree(this);
  }

}
