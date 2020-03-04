package com.adichad.lucense.expression.node.strings;

import java.util.Map;

import org.apache.lucene.util.PriorityQueue;

import com.adichad.lucense.expression.VarContext;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;
import com.adichad.lucense.expression.node.booleans.BooleanExpressionNode;
import com.adichad.lucense.expression.node.booleans.BooleanLiteral;
import com.adichad.lucense.expression.node.ints.IntExpressionNode;
import com.adichad.lucense.expression.node.ints.IntLiteral;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class StringAggregatorFilteredSortedPagedSeperatedConcat implements AggregatorExpressionNode,
    StringExpressionNode {
  StringExpressionNode child;

  // StringBuilder sb = new StringBuilder();
  String seperator;

  private BooleanExpressionNode filter;

  int offset;

  int limit;

  private IntExpressionNode sort;

  private boolean ascending;

  class IntPair<K> {
    K key;

    int val;

    public IntPair(K key, int val) {
      this.key = key;
      this.val = val;
    }

    public K getKey() {
      return this.key;
    }

    public int getValue() {
      return this.val;
    }
  }

  abstract class MyPriorityQueue<T> extends PriorityQueue<T> {
    public MyPriorityQueue(int size) {
      initialize(size);
    }
  }

  // private MyPriorityQueue<IntPair<String, Integer>> pq;

  public StringAggregatorFilteredSortedPagedSeperatedConcat(StringExpressionNode child, StringLiteral seperator,
      IntExpressionNode sort, BooleanLiteral ascending, BooleanExpressionNode filter, IntLiteral offset,
      IntLiteral limit) {
    this.child = child;
    this.seperator = seperator.evaluate(null, null, null, null, null, null);
    this.filter = filter;
    this.offset = offset.evaluate(null, null, null, null, null, null);
    this.limit = limit.evaluate(null, null, null, null, null, null);
    this.sort = sort;
    this.ascending = ascending.evaluate(null, null, null, null, null, null);

  }

  @Override
  public String evaluate(VarContext context, Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    StringAggregatorFilteredSortedPagedSeperatedConcatState state = (StringAggregatorFilteredSortedPagedSeperatedConcatState) slateObject
        .get(this);
    if (this.filter.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean)) {
      int sortby = this.sort.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
      String val = this.child.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
      state.pq.insertWithOverflow(new IntPair<String>(val, sortby));
    }
    return null;
  }

  @Override
  public String evaluateFinal(Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    StringAggregatorFilteredSortedPagedSeperatedConcatState state = (StringAggregatorFilteredSortedPagedSeperatedConcatState) slateObject
        .get(this);
    int count = 0;
    while ((state.pq.size() > 0) && (count < this.offset))
      state.pq.pop();
    count = 0;
    while ((state.pq.size() > 0) && (count < this.limit)) {
      if (state.sb.length() > 0)
        state.sb.append(this.seperator);
      state.sb.append(state.pq.pop().getKey());
    }
    return state.sb.toString();
  }

  class StringAggregatorFilteredSortedPagedSeperatedConcatState {
    StringBuilder sb = new StringBuilder();

    MyPriorityQueue<IntPair<String>> pq = StringAggregatorFilteredSortedPagedSeperatedConcat.this.ascending ? new MyPriorityQueue<IntPair<String>>(
        StringAggregatorFilteredSortedPagedSeperatedConcat.this.offset
            + StringAggregatorFilteredSortedPagedSeperatedConcat.this.limit) {
      @Override
      protected boolean lessThan(IntPair<String> a, IntPair<String> b) {
        return a.getValue() < b.getValue();
      }
    } : new MyPriorityQueue<IntPair<String>>(StringAggregatorFilteredSortedPagedSeperatedConcat.this.offset
        + StringAggregatorFilteredSortedPagedSeperatedConcat.this.limit) {
      @Override
      protected boolean lessThan(IntPair<String> a, IntPair<String> b) {
        return a.getValue() > b.getValue();
      }
    };
  }

  @Override
  public void reset() {

  }

  @Override
  public void initSlate(Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    // TODO Auto-generated method stub
    slateObject.put(this, new StringAggregatorFilteredSortedPagedSeperatedConcatState());
  }

  @Override
  public void updateState(VarContext context, Map<AggregatorExpressionNode, Object> slateObject,
      Object2IntOpenHashMap<AggregatorExpressionNode> slateInt,
      Object2FloatOpenHashMap<AggregatorExpressionNode> slateFloat,
      Object2DoubleOpenHashMap<AggregatorExpressionNode> slateDouble,
      Object2BooleanOpenHashMap<AggregatorExpressionNode> slateBoolean) {
    // TODO Auto-generated method stub

    StringAggregatorFilteredSortedPagedSeperatedConcatState state = (StringAggregatorFilteredSortedPagedSeperatedConcatState) slateObject
        .get(this);
    if (this.filter.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean)) {
      int sortby = this.sort.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
      String val = this.child.evaluate(context, slateObject, slateInt, slateFloat, slateDouble, slateBoolean);
      state.pq.insertWithOverflow(new IntPair<String>(val, sortby));
    }
  }

}
