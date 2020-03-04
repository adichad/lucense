package org.apache.lucene.search;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class QueryLenComparatorSource extends FieldComparatorSource {

  private static final long serialVersionUID = 1L;

  private Object2IntOpenHashMap<String> qlenMap;

  public QueryLenComparatorSource(Object2IntOpenHashMap<String> qlenMap) {
    this.qlenMap = qlenMap;

  }

  public class QueryLenComparator extends FieldComparator<Integer> {
    private final int[] values;

    private int bottom; // Value of bottom of queue

    private String fieldname;

    private Object2IntOpenHashMap<String> qlenMap;

    public QueryLenComparator(String fieldname, int numHits, Object2IntOpenHashMap<String> qlenMap) throws IOException {
      this.values = new int[numHits];
      this.fieldname = fieldname;
      this.qlenMap = qlenMap;
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
    public int compareBottom(int doc) {
      // TODO: there are sneaky non-branch ways to compute
      // -1/+1/0 sign
      // Cannot return bottom - values[slot2] because that
      // may overflow
      Integer nw = this.fieldname.equals("@max") ? getMax(this.qlenMap) : this.qlenMap.get(this.fieldname);
      final int v2 = (nw == null) ? 0 : nw;
      if (this.bottom > v2) {
        return 1;
      } else if (this.bottom < v2) {
        return -1;
      } else {
        return 0;
      }
    }

    @Override
    public void copy(int slot, int doc) {
      Integer nw = this.fieldname.equals("@max") ? getMax(this.qlenMap) : this.qlenMap.get(this.fieldname);
      this.values[slot] = (nw == null) ? 0 : nw;
    }

    private Integer getMax(Object2IntOpenHashMap<String> qlenMap) {
      int max = 0;
      for (int curr : qlenMap.values()) {
        if (curr > max)
          max = curr;
      }
      return max;
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase) throws IOException {}

    @Override
    public void setBottom(final int bottom) {
      this.bottom = this.values[bottom];
    }

    @Override
    public Integer value(int slot) {
      return this.values[slot];
    }

    @Override
    public void setScorer(Scorer scorer) {}

  }

  @Override
  public FieldComparator<Integer> newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
      throws IOException {
    return new QueryLenComparator(fieldname, numHits, this.qlenMap);
  }

}
