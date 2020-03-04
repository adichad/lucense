package org.apache.lucene.search;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.IndexReader;

import com.adichad.lucense.expression.fieldSource.ValueSourceFactory;

public class NumwordsComparatorSource extends FieldComparatorSource {

  private static final long serialVersionUID = 1L;

  public class NumwordsComparator extends FieldComparator<Integer> {
    private final int[] values;

    private int bottom; // Value of bottom of queue

    private String fieldname;

    private CustomScorer scorer;

    public NumwordsComparator(String fieldname, int numHits) throws IOException {
      this.values = new int[numHits];
      this.fieldname = fieldname;
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
      Map<String, Integer> nmap;
      try {
        nmap = this.scorer.numwords(ValueSourceFactory.scoreFields);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      final int v2 = nmap.get(this.fieldname);
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
      Map<String, Integer> nmap;
      try {
        nmap = this.scorer.numwords(ValueSourceFactory.scoreFields);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      this.values[slot] = nmap.get(this.fieldname);
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
    public void setScorer(Scorer scorer) {
      this.scorer = (CustomScorer) scorer;
    }

  }

  @Override
  public FieldComparator<Integer> newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
      throws IOException {
    return new NumwordsComparator(fieldname, numHits);
  }

}
