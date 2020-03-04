package com.adichad.lucense.expression;

import java.util.List;
import java.util.Set;

import com.adichad.lucense.expression.node.AggregatorExpressionNode;
import com.adichad.lucense.expression.node.strings.StringExpressionNode;

public class StringAggregatingExpressionTree extends StringExpressionTree {
  protected List<AggregatorExpressionNode> aggregateNodes;

  public StringAggregatingExpressionTree(StringExpressionNode root, Set<String> intVars, Set<String> floatVars,
      Set<String> doubleVars, Set<String> booleanVars, Set<String> stringVars,
      List<AggregatorExpressionNode> aggregateNodes) {
    super(root, intVars, floatVars, doubleVars, booleanVars, stringVars);
    this.aggregateNodes = aggregateNodes;

    for (AggregatorExpressionNode key : aggregateNodes) {
      key.initSlate(this.slateObject, this.slateInt, this.slateFloat, this.slateDouble, this.slateBoolean);
    }
  }

  @Override
  public String evaluate() {
    for (AggregatorExpressionNode node : this.aggregateNodes) {
      node.updateState(this.context, this.slateObject, this.slateInt, this.slateFloat, this.slateDouble,
          this.slateBoolean);
    }
    return null;
  }

  @Override
  public String evaluateFinal() {
    return ((StringExpressionNode) this.root).evaluateFinal(this.slateObject, this.slateInt, this.slateFloat,
        this.slateDouble, this.slateBoolean);
  }

  protected StringAggregatingExpressionTree(StringAggregatingExpressionTree tree) {
    super(tree);
    this.aggregateNodes = tree.aggregateNodes;

    for (AggregatorExpressionNode key : this.aggregateNodes) {
      key.initSlate(this.slateObject, this.slateInt, this.slateFloat, this.slateDouble, this.slateBoolean);
    }
  }

  @Override
  public StringAggregatingExpressionTree clone() {
    return new StringAggregatingExpressionTree(this);
  }

}
