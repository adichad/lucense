package org.apache.lucene.search;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.IndexReader;

public class IndexBoostComparatorSource extends FieldComparatorSource {

  private static final long serialVersionUID = 1L;

  Map<String, Float> readerBoosts;

  public IndexBoostComparatorSource(Map<String, Float> readerBoosts) {
    this.readerBoosts = readerBoosts;
  }

  public class IndexBoostComparator extends FieldComparator<Float> {
    private final float[] values;

    private float bottom; // Value of bottom of queue

    private float currentReaderBoost;

    private Map<String, Float> readerBoosts;

    public IndexBoostComparator(Map<String, Float> readerBoosts, int numHits) throws IOException {
      this.values = new float[numHits];
      this.readerBoosts = readerBoosts;
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
    public int compareBottom(int doc) {
      // TODO: there are sneaky non-branch ways to compute
      // -1/+1/0 sign
      // Cannot return bottom - values[slot2] because that
      // may overflow
      final float v2 = this.currentReaderBoost;
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
      this.values[slot] = this.currentReaderBoost;
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase) throws IOException {
      this.currentReaderBoost = this.readerBoosts.get(reader.directory().toString());
    }

    @Override
    public void setBottom(final int bottom) {
      this.bottom = this.values[bottom];
    }

    @Override
    public Float value(int slot) {
      return this.values[slot];
    }

    @Override
    public void setScorer(Scorer scorer) {}

  }

  @Override
  public FieldComparator<Float> newComparator(String fieldname, int numHits, int sortPos, boolean reversed)
      throws IOException {
    return new IndexBoostComparator(this.readerBoosts, numHits);
  }

}
