package com.adichad.lucense.expression;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.Scorer;
import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.fieldSource.FieldType;

public class ExpressionComparatorSource extends FieldComparatorSource {

  private static final long serialVersionUID = 1L;

  private LucenseExpression expr;

  public ExpressionComparatorSource(LucenseExpression expr) {
    this.expr = expr;
  }

  public class IntExpressionComparator extends FieldComparator<Integer> {
    IntLucenseExpression      expression;

    protected final int[]     values;

    protected int             bottom;    // Value of bottom of queue

    protected final boolean[] finalized;

    protected final Slates    state;

    protected Context         cx;

    public IntExpressionComparator(IntLucenseExpression e, Context cx,
        int numHits) throws IOException {
      this.expression = e;
      this.state = e.initState(cx);
      this.values = new int[numHits];
      this.finalized = new boolean[numHits];
      this.cx = cx;
    }

    @Override
    public int compare(int slot1, int slot2) {
      // TODO: there are sneaky non-branch ways to compute
      // -1/+1/0 sign
      // Cannot return values[slot1] - values[slot2] because that
      // may overflow
      final int v1 = this.values[slot1];
      final int v2 = this.values[slot2];
      if (v1 > v2) {
        return 1;
      } else if (v1 < v2) {
        return -1;
      } else {
        return 0;
      }
    }

    @Override
    public int compareBottom(int doc) throws IOException {
      // TODO: there are sneaky non-branch ways to compute
      // -1/+1/0 sign
      // Cannot return bottom - values[slot2] because that
      // may overflow
      final int v2 = this.expression.evaluate(doc, state, cx);
      if (this.bottom > v2) {
        return 1;
      } else if (this.bottom < v2) {
        return -1;
      } else {
        return 0;
      }
    }

    @Override
    public void copy(int slot, int doc) throws IOException {
      this.values[slot] = this.expression.evaluate(doc, state, cx);

    }

    @Override
    public void setNextReader(IndexReader reader, int docBase)
        throws IOException {
      this.expression.setNextReader(reader, docBase);
    }

    @Override
    public void setBottom(final int bottom) {
      this.bottom = this.values[bottom];
    }

    @Override
    public Integer value(int slot) {
      if ((expression.getExpressionTree() instanceof IntAggregatingExpressionTree)
          && !this.finalized[slot]) {
        this.values[slot] = this.expression.evaluateFinal(state, cx);
        this.finalized[slot] = true;
      }
      return this.values[slot];
    }

    @Override
    public void setScorer(Scorer scorer) {
      this.expression.setScorer(scorer);
    }

  }

  public class ReversedIntExpressionComparator extends IntExpressionComparator {

    public ReversedIntExpressionComparator(IntLucenseExpression e, Context cx,
        int numHits) throws IOException {
      super(e, cx, numHits);
    }

    @Override
    public int compare(int slot1, int slot2) {
      // TODO: there are sneaky non-branch ways to compute
      // -1/+1/0 sign
      // Cannot return values[slot1] - values[slot2] because that
      // may overflow
      final int v1 = this.values[slot1];
      final int v2 = this.values[slot2];
      if (v1 < v2) {
        return 1;
      } else if (v1 > v2) {
        return -1;
      } else {
        return 0;
      }
    }

    @Override
    public int compareBottom(int doc) throws IOException {
      // TODO: there are sneaky non-branch ways to compute
      // -1/+1/0 sign
      // Cannot return bottom - values[slot2] because that
      // may overflow
      final int v2 = this.expression.evaluate(doc, state, cx);
      if (this.bottom < v2) {
        return 1;
      } else if (this.bottom > v2) {
        return -1;
      } else {
        return 0;
      }
    }

  }

  public class BooleanExpressionComparator extends FieldComparator<Boolean> {
    BooleanLucenseExpression  expression;

    protected final byte[]    values;

    protected byte            bottom;    // Value of bottom of queue

    protected final boolean[] finalized;

    protected Slates          state;

    protected Context         cx;

    public BooleanExpressionComparator(BooleanLucenseExpression e, Context cx,
        int numHits) throws IOException {
      this.expression = e;
      this.state = e.initState(cx);
      this.values = new byte[numHits];
      this.finalized = new boolean[numHits];
      this.cx = cx;
    }

    @Override
    public int compare(int slot1, int slot2) {
      // TODO: there are sneaky non-branch ways to compute
      // -1/+1/0 sign
      // Cannot return values[slot1] - values[slot2] because that
      // may overflow
      final byte v1 = this.values[slot1];
      final byte v2 = this.values[slot2];
      if (v1 > v2) {
        return 1;
      } else if (v1 < v2) {
        return -1;
      } else {
        return 0;
      }
    }

    @Override
    public int compareBottom(int doc) throws IOException {
      // TODO: there are sneaky non-branch ways to compute
      // -1/+1/0 sign
      // Cannot return bottom - values[slot2] because that
      // may overflow
      final byte v2 = (byte) (this.expression.evaluate(doc, state, cx) ? 1 : 0);

      if (this.bottom > v2) {
        return 1;
      } else if (this.bottom < v2) {
        return -1;
      } else {
        return 0;
      }
    }

    @Override
    public void copy(int slot, int doc) throws IOException {
      try {
        Boolean b = this.expression.evaluate(doc, state, cx);

        if ((b != null) && b)
          this.values[slot] = 1;
        else
          this.values[slot] = 0;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase)
        throws IOException {
      this.expression.setNextReader(reader, docBase);
    }

    @Override
    public void setBottom(final int bottom) {
      this.bottom = this.values[bottom];
    }

    @Override
    public Boolean value(int slot) {
      if ((expression.getExpressionTree() instanceof BooleanAggregatingExpressionTree)
          && !this.finalized[slot]) {
        Boolean b = this.expression.evaluateFinal(state, cx);

        if ((b != null) && b)
          this.values[slot] = 1;
        else
          this.values[slot] = 0;
        this.finalized[slot] = true;
      }
      return this.values[slot] == 1 ? true : false;
    }

    @Override
    public void setScorer(Scorer scorer) {
      this.expression.setScorer(scorer);
    }

  }

  public class ReversedBooleanExpressionComparator extends
      BooleanExpressionComparator {

    public ReversedBooleanExpressionComparator(BooleanLucenseExpression e,
        Context cx, int numHits) throws IOException {
      super(e, cx, numHits);
    }

    @Override
    public int compare(int slot1, int slot2) {
      // TODO: there are sneaky non-branch ways to compute
      // -1/+1/0 sign
      // Cannot return values[slot1] - values[slot2] because that
      // may overflow
      final byte v1 = this.values[slot1];
      final byte v2 = this.values[slot2];
      if (v1 < v2) {
        return 1;
      } else if (v1 > v2) {
        return -1;
      } else {
        return 0;
      }
    }

    @Override
    public int compareBottom(int doc) throws IOException {
      // TODO: there are sneaky non-branch ways to compute
      // -1/+1/0 sign
      // Cannot return bottom - values[slot2] because that
      // may overflow
      final byte v2 = (byte) (this.expression.evaluate(doc, state, cx) ? 1 : 0);

      if (this.bottom < v2) {
        return 1;
      } else if (this.bottom > v2) {
        return -1;
      } else {
        return 0;
      }
    }

  }

  public class StringExpressionComparator extends FieldComparator<String> {
    StringLucenseExpression   expression;

    protected final String[]  values;

    protected String          bottom;    // Value of bottom of queue

    protected final boolean[] finalized;

    protected final Slates    state;

    protected Context         cx;

    public StringExpressionComparator(StringLucenseExpression e, Context cx,
        int numHits) throws IOException {
      this.expression = e;
      this.state = e.initState(cx);
      this.values = new String[numHits];
      this.finalized = new boolean[numHits];
      this.cx = cx;
    }

    @Override
    public int compare(int slot1, int slot2) {
      // TODO: there are sneaky non-branch ways to compute
      // -1/+1/0 sign
      // Cannot return values[slot1] - values[slot2] because that
      // may overflow
      final String v1 = this.values[slot1];
      final String v2 = this.values[slot2];
      return v1.compareTo(v2);
    }

    @Override
    public int compareBottom(int doc) throws IOException {
      // TODO: there are sneaky non-branch ways to compute
      // -1/+1/0 sign
      // Cannot return bottom - values[slot2] because that
      // may overflow
      final String v2 = this.expression.evaluate(doc, state, cx);
      return this.bottom.compareTo(v2);
    }

    @Override
    public void copy(int slot, int doc) throws IOException {
      this.values[slot] = this.expression.evaluate(doc, state, cx);
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase)
        throws IOException {
      this.expression.setNextReader(reader, docBase);
    }

    @Override
    public void setBottom(final int bottom) {
      this.bottom = this.values[bottom];
    }

    @Override
    public String value(int slot) {
      if ((expression.getExpressionTree() instanceof StringAggregatingExpressionTree)
          && !this.finalized[slot]) {
        this.values[slot] = this.expression.evaluateFinal(state, cx);
        this.finalized[slot] = true;
      }
      return this.values[slot];
    }

    @Override
    public void setScorer(Scorer scorer) {
      this.expression.setScorer(scorer);
    }

  }

  public class ReversedStringExpressionComparator extends
      StringExpressionComparator {

    public ReversedStringExpressionComparator(StringLucenseExpression e,
        Context cx, int numHits) throws IOException {
      super(e, cx, numHits);

    }

    @Override
    public int compare(int slot1, int slot2) {
      // TODO: there are sneaky non-branch ways to compute
      // -1/+1/0 sign
      // Cannot return values[slot1] - values[slot2] because that
      // may overflow
      final String v1 = this.values[slot1];
      final String v2 = this.values[slot2];
      return v2.compareTo(v1);
    }

    @Override
    public int compareBottom(int doc) throws IOException {
      // TODO: there are sneaky non-branch ways to compute
      // -1/+1/0 sign
      // Cannot return bottom - values[slot2] because that
      // may overflow
      final String v2 = this.expression.evaluate(doc, state, cx);
      return v2.compareTo(this.bottom);
    }

  }

  public class FloatExpressionComparator extends FieldComparator<Float> {
    FloatLucenseExpression    expression;

    protected final float[]   values;

    protected float           bottom;    // Value of bottom of queue

    protected final boolean[] finalized;

    protected final Slates    state;

    protected Context         cx;

    public FloatExpressionComparator(FloatLucenseExpression e, Context cx,
        int numHits) throws IOException {
      this.expression = e;
      this.state = e.initState(cx);
      this.values = new float[numHits];
      this.finalized = new boolean[numHits];
      this.cx = cx;
    }

    @Override
    public int compare(int slot1, int slot2) {
      // TODO: there are sneaky non-branch ways to compute
      // -1/+1/0 sign
      // Cannot return values[slot1] - values[slot2] because that
      // may overflow
      final float v1 = this.values[slot1];
      final float v2 = this.values[slot2];
      if (v1 > v2) {
        return 1;
      } else if (v1 < v2) {
        return -1;
      } else {
        return 0;
      }
    }

    @Override
    public int compareBottom(int doc) throws IOException {
      // TODO: there are sneaky non-branch ways to compute
      // -1/+1/0 sign
      // Cannot return bottom - values[slot2] because that
      // may overflow
      final float v2 = this.expression.evaluate(doc, state, cx);
      if (this.bottom > v2) {
        return 1;
      } else if (this.bottom < v2) {
        return -1;
      } else {
        return 0;
      }
    }

    @Override
    public void copy(int slot, int doc) throws IOException {
      this.values[slot] = this.expression.evaluate(doc, state, cx);
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase)
        throws IOException {
      this.expression.setNextReader(reader, docBase);
    }

    @Override
    public void setBottom(final int bottom) {
      this.bottom = this.values[bottom];
    }

    @Override
    public Float value(int slot) {
      if ((expression.getExpressionTree() instanceof FloatAggregatingExpressionTree)
          && !this.finalized[slot]) {
        this.values[slot] = this.expression.evaluateFinal(state, cx);
        this.finalized[slot] = true;
      }
      return this.values[slot];
      // return values[slot];
    }

    @Override
    public void setScorer(Scorer scorer) {
      this.expression.setScorer(scorer);
    }

  }

  public class ReversedFloatExpressionComparator extends
      FloatExpressionComparator {

    public ReversedFloatExpressionComparator(FloatLucenseExpression e,
        Context cx, int numHits) throws IOException {
      super(e, cx, numHits);
    }

    @Override
    public int compare(int slot1, int slot2) {
      // TODO: there are sneaky non-branch ways to compute
      // -1/+1/0 sign
      // Cannot return values[slot1] - values[slot2] because that
      // may overflow
      final float v1 = this.values[slot1];
      final float v2 = this.values[slot2];
      if (v1 < v2) {
        return 1;
      } else if (v1 > v2) {
        return -1;
      } else {
        return 0;
      }
    }

    @Override
    public int compareBottom(int doc) throws IOException {
      // TODO: there are sneaky non-branch ways to compute
      // -1/+1/0 sign
      // Cannot return bottom - values[slot2] because that
      // may overflow
      final float v2 = this.expression.evaluate(doc, state, cx);
      if (this.bottom < v2) {
        return 1;
      } else if (this.bottom > v2) {
        return -1;
      } else {
        return 0;
      }
    }

  }

  public class DoubleExpressionComparator extends FieldComparator<Double> {
    DoubleLucenseExpression   expression;

    protected final double[]  values;

    protected double          bottom;    // Value of bottom of queue

    protected final boolean[] finalized;

    protected final Slates    state;

    protected Context         cx;

    public DoubleExpressionComparator(DoubleLucenseExpression e, Context cx,
        int numHits) throws IOException {
      this.expression = e;
      this.state = e.initState(cx);
      this.values = new double[numHits];
      this.finalized = new boolean[numHits];
      this.cx = cx;
    }

    @Override
    public int compare(int slot1, int slot2) {
      // TODO: there are sneaky non-branch ways to compute
      // -1/+1/0 sign
      // Cannot return values[slot1] - values[slot2] because that
      // may overflow
      final double v1 = this.values[slot1];
      final double v2 = this.values[slot2];
      if (v1 > v2) {
        return 1;
      } else if (v1 < v2) {
        return -1;
      } else {
        return 0;
      }
    }

    @Override
    public int compareBottom(int doc) throws IOException {
      // TODO: there are sneaky non-branch ways to compute
      // -1/+1/0 sign
      // Cannot return bottom - values[slot2] because that
      // may overflow
      final double v2 = this.expression.evaluate(doc, state, cx);
      if (this.bottom > v2) {
        return 1;
      } else if (this.bottom < v2) {
        return -1;
      } else {
        return 0;
      }
    }

    @Override
    public void copy(int slot, int doc) throws IOException {
      this.values[slot] = this.expression.evaluate(doc, state, cx);
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase)
        throws IOException {
      this.expression.setNextReader(reader, docBase);
    }

    @Override
    public void setBottom(final int bottom) {
      this.bottom = this.values[bottom];
    }

    @Override
    public Double value(int slot) {
      if ((expression.getExpressionTree() instanceof DoubleAggregatingExpressionTree)
          && !this.finalized[slot]) {
        this.values[slot] = this.expression.evaluateFinal(state, cx);
        this.finalized[slot] = true;
      }
      return this.values[slot];
    }

    @Override
    public void setScorer(Scorer scorer) {
      this.expression.setScorer(scorer);
    }
  }

  public class ReversedDoubleExpressionComparator extends
      DoubleExpressionComparator {

    public ReversedDoubleExpressionComparator(DoubleLucenseExpression e,
        Context cx, int numHits) throws IOException {
      super(e, cx, numHits);
    }

    @Override
    public int compare(int slot1, int slot2) {
      // TODO: there are sneaky non-branch ways to compute
      // -1/+1/0 sign
      // Cannot return values[slot1] - values[slot2] because that
      // may overflow
      final double v1 = this.values[slot1];
      final double v2 = this.values[slot2];
      if (v1 < v2) {
        return 1;
      } else if (v1 > v2) {
        return -1;
      } else {
        return 0;
      }
    }

    @Override
    public int compareBottom(int doc) throws IOException {
      // TODO: there are sneaky non-branch ways to compute
      // -1/+1/0 sign
      // Cannot return bottom - values[slot2] because that
      // may overflow
      final double v2 = this.expression.evaluate(doc, state, cx);
      if (this.bottom < v2) {
        return 1;
      } else if (this.bottom > v2) {
        return -1;
      } else {
        return 0;
      }
    }

  }

  @Override
  public FieldComparator<?> newComparator(String fieldname, int numHits,
      int sortPos, boolean reversed) throws IOException {
    
    //System.out.println(fieldname+"("+expr.getType()+"): reversed: "+reversed);
    Context cx = Context.enter();
    try {
      if (reversed) {
        switch (expr.getType()) {
        case TYPE_DOUBLE:
          return new ReversedDoubleExpressionComparator(
              (DoubleLucenseExpression) this.expr, cx, numHits);
        case TYPE_FLOAT:
          return new ReversedFloatExpressionComparator(
              (FloatLucenseExpression) this.expr, cx, numHits);
        case TYPE_STRING:
          return new ReversedStringExpressionComparator(
              (StringLucenseExpression) this.expr, cx, numHits);
        case TYPE_INT:
          return new ReversedIntExpressionComparator(
              (IntLucenseExpression) this.expr, cx, numHits);
        case TYPE_BOOLEAN:
          return new ReversedBooleanExpressionComparator(
              (BooleanLucenseExpression) this.expr, cx, numHits);
        default:
          return new ReversedIntExpressionComparator(
              (IntLucenseExpression) this.expr, cx, numHits);
        }

      } else {
        switch (expr.getType()) {
        case TYPE_DOUBLE:
          return new DoubleExpressionComparator(
              (DoubleLucenseExpression) this.expr, cx, numHits);
        case TYPE_FLOAT:
          return new FloatExpressionComparator(
              (FloatLucenseExpression) this.expr, cx, numHits);
        case TYPE_STRING:
          return new StringExpressionComparator(
              (StringLucenseExpression) this.expr, cx, numHits);
        case TYPE_INT:
          return new IntExpressionComparator((IntLucenseExpression) this.expr,
              cx, numHits);
        case TYPE_BOOLEAN:
          return new BooleanExpressionComparator(
              (BooleanLucenseExpression) this.expr, cx, numHits);
        default:
          return new IntExpressionComparator((IntLucenseExpression) this.expr,
              cx, numHits);
        }
      }
    } finally {
      Context.exit();
    }
  }

  public FieldType getExprType() {
    return this.expr.getType();
  }

}
