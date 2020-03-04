package com.adichad.lucense.expression;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.Scorer;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.adichad.lucense.expression.fieldSource.BooleanValueSource;
import com.adichad.lucense.expression.fieldSource.DoubleValueSource;
import com.adichad.lucense.expression.fieldSource.FloatValueSource;
import com.adichad.lucense.expression.fieldSource.IntValueSource;
import com.adichad.lucense.expression.fieldSource.StringValueSource;
import com.adichad.lucense.request.Request.FieldType;
import com.adichad.lucense.resource.SearchResourceManager;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class ExpressionComparatorSource extends FieldComparatorSource {

  private static final long serialVersionUID = 1L;

  private Context cx;

  private Scriptable scope;

  private FieldType exprType;

  private Map<String, Object2IntOpenHashMap<String>> externalValSource;

  private Map<String, LucenseExpression> namedExprs;

  LucenseExpression precompiledExpr = null;

  private Map<String, IntValueSource> intValueSources;

  private Map<String, FloatValueSource> floatValueSources;

  private Map<String, DoubleValueSource> doubleValueSources;

  private Map<String, BooleanValueSource> booleanValueSources;

  private Map<String, StringValueSource> stringValueSources;

  private SearchResourceManager srm;

  public ExpressionComparatorSource(FieldType exprType, Context cx, Scriptable scope,
      Map<String, Object2IntOpenHashMap<String>> externalValSource, Map<String, LucenseExpression> namedExprs,
      Map<String, IntValueSource> intValueSources, Map<String, FloatValueSource> floatValueSources,
      Map<String, DoubleValueSource> doubleValueSources, Map<String, BooleanValueSource> booleanValueSources,
      Map<String, StringValueSource> stringValueSources, SearchResourceManager srm) {
    this.cx = cx;
    this.scope = scope;
    this.exprType = exprType;
    this.externalValSource = externalValSource;
    this.intValueSources = intValueSources;
    this.floatValueSources = floatValueSources;
    this.doubleValueSources = doubleValueSources;
    this.booleanValueSources = booleanValueSources;
    this.stringValueSources = stringValueSources;
    this.namedExprs = namedExprs;
    this.srm = srm;
  }

  public class IntExpressionComparator extends FieldComparator<Integer> {
    IntLucenseExpression expression;

    private final int[] values;

    private int bottom; // Value of bottom of queue

    private final boolean[] finalized;

    public IntExpressionComparator(IntLucenseExpression e, int numHits) throws IOException {
      this.expression = e;
      this.values = new int[numHits];
      this.finalized = new boolean[numHits];
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
      final int v2 = this.expression.evaluate(doc);
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
      this.values[slot] = this.expression.evaluate(doc);

    }

    @Override
    public void setNextReader(IndexReader reader, int docBase) throws IOException {
      this.expression.setNextReader(reader, docBase);
    }

    @Override
    public void setBottom(final int bottom) {
      this.bottom = this.values[bottom];
    }

    @Override
    public Integer value(int slot) {
      if ((expression.getExpressionTree() instanceof IntAggregatingExpressionTree) && !this.finalized[slot]) {
        this.values[slot] = this.expression.evaluateFinal();
        this.finalized[slot] = true;
      }
      return this.values[slot];
    }

    @Override
    public void setScorer(Scorer scorer) {
      this.expression.setScorer(scorer);
    }

  }

  public class BooleanExpressionComparator extends FieldComparator<Boolean> {
    BooleanLucenseExpression expression;

    private final byte[] values;

    private byte bottom; // Value of bottom of queue

    private final boolean[] finalized;

    public BooleanExpressionComparator(BooleanLucenseExpression e, int numHits) throws IOException {
      this.expression = e;
      this.values = new byte[numHits];
      this.finalized = new boolean[numHits];
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
      final byte v2 = (byte) (this.expression.evaluate(doc) ? 1 : 0);

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
        Boolean b = this.expression.evaluate(doc);

        if ((b != null) && b)
          this.values[slot] = 1;
        else
          this.values[slot] = 0;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase) throws IOException {
      this.expression.setNextReader(reader, docBase);
    }

    @Override
    public void setBottom(final int bottom) {
      this.bottom = this.values[bottom];
    }

    @Override
    public Boolean value(int slot) {
      if ((expression.getExpressionTree() instanceof BooleanAggregatingExpressionTree) && !this.finalized[slot]) {
        Boolean b = this.expression.evaluateFinal();

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

  public class StringExpressionComparator extends FieldComparator<String> {
    StringLucenseExpression expression;

    private final String[] values;

    private String bottom; // Value of bottom of queue

    private final boolean[] finalized;

    public StringExpressionComparator(StringLucenseExpression e, int numHits) throws IOException {
      this.expression = e;
      this.values = new String[numHits];
      this.finalized = new boolean[numHits];
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
      final String v2 = this.expression.evaluate(doc);
      return this.bottom.compareTo(v2);
    }

    @Override
    public void copy(int slot, int doc) throws IOException {
      this.values[slot] = this.expression.evaluate(doc);
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase) throws IOException {
      this.expression.setNextReader(reader, docBase);
    }

    @Override
    public void setBottom(final int bottom) {
      this.bottom = this.values[bottom];
    }

    @Override
    public String value(int slot) {
      if ((expression.getExpressionTree() instanceof StringAggregatingExpressionTree) && !this.finalized[slot]) {
        this.values[slot] = this.expression.evaluateFinal();
        this.finalized[slot] = true;
      }
      return this.values[slot];
    }

    @Override
    public void setScorer(Scorer scorer) {
      this.expression.setScorer(scorer);
    }

  }

  public class FloatExpressionComparator extends FieldComparator<Float> {
    FloatLucenseExpression expression;

    private final float[] values;

    private float bottom; // Value of bottom of queue

    private final boolean[] finalized;

    public FloatExpressionComparator(FloatLucenseExpression e, int numHits) throws IOException {
      this.expression = e;
      this.values = new float[numHits];
      this.finalized = new boolean[numHits];
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
      final float v2 = this.expression.evaluate(doc);
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
      this.values[slot] = this.expression.evaluate(doc);
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase) throws IOException {
      this.expression.setNextReader(reader, docBase);
    }

    @Override
    public void setBottom(final int bottom) {
      this.bottom = this.values[bottom];
    }

    @Override
    public Float value(int slot) {
      if ((expression.getExpressionTree() instanceof FloatAggregatingExpressionTree) && !this.finalized[slot]) {
        this.values[slot] = this.expression.evaluateFinal();
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

  public class DoubleExpressionComparator extends FieldComparator<Double> {
    DoubleLucenseExpression expression;

    private final double[] values;

    private double bottom; // Value of bottom of queue

    private final boolean[] finalized;

    public DoubleExpressionComparator(DoubleLucenseExpression e, int numHits) throws IOException {
      this.expression = e;
      this.values = new double[numHits];
      this.finalized = new boolean[numHits];
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
      final double v2 = this.expression.evaluate(doc);
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
      this.values[slot] = this.expression.evaluate(doc);
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase) throws IOException {
      this.expression.setNextReader(reader, docBase);
    }

    @Override
    public void setBottom(final int bottom) {
      this.bottom = this.values[bottom];
    }

    @Override
    public Double value(int slot) {
      if ((expression.getExpressionTree() instanceof DoubleAggregatingExpressionTree) && !this.finalized[slot]) {
        this.values[slot] = this.expression.evaluateFinal();
        this.finalized[slot] = true;
      }
      return this.values[slot];
    }

    @Override
    public void setScorer(Scorer scorer) {
      this.expression.setScorer(scorer);
    }
  }

  @Override
  public FieldComparator<?> newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
      throws IOException {

    switch (this.exprType) {
    case TYPE_DOUBLE:
      if (this.precompiledExpr == null) {
        this.precompiledExpr = ExpressionFactory.getExpressionFromString(fieldname, FieldType.TYPE_DOUBLE, this.cx,
            this.scope, this.externalValSource, this.namedExprs, new ValueSources(this.intValueSources,
                this.floatValueSources, this.doubleValueSources, this.booleanValueSources, this.stringValueSources),
            srm);
      } else {
        this.precompiledExpr = this.precompiledExpr.clone();
      }
      return new DoubleExpressionComparator((DoubleLucenseExpression) this.precompiledExpr, numHits);

    case TYPE_FLOAT:
      if (this.precompiledExpr == null) {
        this.precompiledExpr = ExpressionFactory.getExpressionFromString(fieldname, FieldType.TYPE_FLOAT, this.cx,
            this.scope, this.externalValSource, this.namedExprs, new ValueSources(this.intValueSources,
                this.floatValueSources, this.doubleValueSources, this.booleanValueSources, this.stringValueSources),
            srm);
      } else {
        this.precompiledExpr = this.precompiledExpr.clone();
      }
      return new FloatExpressionComparator((FloatLucenseExpression) this.precompiledExpr, numHits);
    case TYPE_STRING:
      if (this.precompiledExpr == null) {
        this.precompiledExpr = ExpressionFactory.getExpressionFromString(fieldname, FieldType.TYPE_STRING, this.cx,
            this.scope, this.externalValSource, this.namedExprs, new ValueSources(this.intValueSources,
                this.floatValueSources, this.doubleValueSources, this.booleanValueSources, this.stringValueSources),
            srm);
      } else {
        this.precompiledExpr = this.precompiledExpr.clone();
      }
      return new StringExpressionComparator((StringLucenseExpression) this.precompiledExpr, numHits);
    case TYPE_INT:
      if (this.precompiledExpr == null) {
        this.precompiledExpr = ExpressionFactory.getExpressionFromString(fieldname, FieldType.TYPE_INT, this.cx,
            this.scope, this.externalValSource, this.namedExprs, new ValueSources(this.intValueSources,
                this.floatValueSources, this.doubleValueSources, this.booleanValueSources, this.stringValueSources),
            srm);
      } else {
        this.precompiledExpr = this.precompiledExpr.clone();
      }
      return new IntExpressionComparator((IntLucenseExpression) this.precompiledExpr, numHits);
    case TYPE_BOOLEAN:
      if (this.precompiledExpr == null) {
        this.precompiledExpr = ExpressionFactory.getExpressionFromString(fieldname, FieldType.TYPE_BOOLEAN, this.cx,
            this.scope, this.externalValSource, this.namedExprs, new ValueSources(this.intValueSources,
                this.floatValueSources, this.doubleValueSources, this.booleanValueSources, this.stringValueSources),
            srm);
      } else {
        this.precompiledExpr = this.precompiledExpr.clone();
      }
      return new BooleanExpressionComparator((BooleanLucenseExpression) this.precompiledExpr, numHits);

    default:
      if (this.precompiledExpr == null) {
        this.precompiledExpr = ExpressionFactory.getExpressionFromString(fieldname, FieldType.TYPE_INT, this.cx,
            this.scope, this.externalValSource, this.namedExprs, new ValueSources(this.intValueSources,
                this.floatValueSources, this.doubleValueSources, this.booleanValueSources, this.stringValueSources),
            srm);
      } else {
        this.precompiledExpr = this.precompiledExpr.clone();
      }
      return new IntExpressionComparator((IntLucenseExpression) this.precompiledExpr, numHits);
    }

  }

  public FieldType getExprType() {
    return this.exprType;
  }

}
