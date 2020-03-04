package com.adichad.lucense.expression.node.strings;

import org.apache.lucene.util.PriorityQueue;
import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.Slates;
import com.adichad.lucense.expression.node.AggregatorExpressionNode;
import com.adichad.lucense.expression.node.booleans.BooleanExpressionNode;
import com.adichad.lucense.expression.node.booleans.BooleanLiteral;
import com.adichad.lucense.expression.node.ints.IntExpressionNode;
import com.adichad.lucense.expression.node.ints.IntLiteral;

final public class StringAggregatorFilteredSortedPagedSeperatedConcat implements
    AggregatorExpressionNode, StringExpressionNode {
  private final StringExpressionNode  child;

  private final String                seperator;

  private final BooleanExpressionNode filter;

  private final int                   offset;

  private final int                   limit;

  private final IntExpressionNode     sort;

  private final boolean               ascending;

  private final class IntPair<K> {
    private final K   key;

    private final int val;

    public IntPair(K key, int val) {
      this.key = key;
      this.val = val;
    }

    final public K getKey() {
      return this.key;
    }

    final public int getValue() {
      return this.val;
    }
  }

  abstract class MyPriorityQueue<T> extends PriorityQueue<T> {
    public MyPriorityQueue(int size) {
      initialize(size);
    }
  }

  // private MyPriorityQueue<IntPair<String, Integer>> pq;

  public StringAggregatorFilteredSortedPagedSeperatedConcat(
      StringExpressionNode child, StringLiteral seperator,
      IntExpressionNode sort, BooleanLiteral ascending,
      BooleanExpressionNode filter, IntLiteral offset, IntLiteral limit) {
    this.child = child;
    this.seperator = seperator.evaluate(null, null);
    this.filter = filter;
    this.offset = offset.evaluate(null, null);
    this.limit = limit.evaluate(null, null);
    this.sort = sort;
    this.ascending = ascending.evaluate(null, null);

  }

  @Override
  final public String evaluate(Slates slates, Context cx) {
    StringAggregatorFilteredSortedPagedSeperatedConcatState state = (StringAggregatorFilteredSortedPagedSeperatedConcatState) slates.slateObject
        .get(this);
    if (this.filter.evaluate(slates, cx)) {
      int sortby = this.sort.evaluate(slates, cx);
      String val = this.child.evaluate(slates, cx);
      state.pq.insertWithOverflow(new IntPair<String>(val, sortby));
    }
    return null;
  }

  @Override
  final public String evaluateFinal(Slates slates, Context cx) {
    StringAggregatorFilteredSortedPagedSeperatedConcatState state = (StringAggregatorFilteredSortedPagedSeperatedConcatState) slates.slateObject
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

  private final class StringAggregatorFilteredSortedPagedSeperatedConcatState {
    StringBuilder                    sb = new StringBuilder();

    MyPriorityQueue<IntPair<String>> pq = StringAggregatorFilteredSortedPagedSeperatedConcat.this.ascending ? new MyPriorityQueue<IntPair<String>>(
                                            StringAggregatorFilteredSortedPagedSeperatedConcat.this.offset
                                                + StringAggregatorFilteredSortedPagedSeperatedConcat.this.limit) {
                                          @Override
                                          protected boolean lessThan(
                                              IntPair<String> a,
                                              IntPair<String> b) {
                                            return a.getValue() < b.getValue();
                                          }
                                        }
                                            : new MyPriorityQueue<IntPair<String>>(
                                                StringAggregatorFilteredSortedPagedSeperatedConcat.this.offset
                                                    + StringAggregatorFilteredSortedPagedSeperatedConcat.this.limit) {
                                              @Override
                                              protected boolean lessThan(
                                                  IntPair<String> a,
                                                  IntPair<String> b) {
                                                return a.getValue() > b
                                                    .getValue();
                                              }
                                            };
  }

  @Override
  final public void initSlate(Slates slates, Context cx) {
    slates.slateObject.put(this,
        new StringAggregatorFilteredSortedPagedSeperatedConcatState());
  }

  @Override
  final public void updateState(Slates slates, Context cx) {
    StringAggregatorFilteredSortedPagedSeperatedConcatState state = (StringAggregatorFilteredSortedPagedSeperatedConcatState) slates.slateObject
        .get(this);
    if (this.filter.evaluate(slates, cx)) {
      int sortby = this.sort.evaluate(slates, cx);
      String val = this.child.evaluate(slates, cx);
      state.pq.insertWithOverflow(new IntPair<String>(val, sortby));
    }
  }

}
