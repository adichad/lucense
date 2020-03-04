package org.apache.lucene.search;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.IndexReader;

/* Description from Doug Cutting (excerpted from
 * LUCENE-1483):
 *
 * BooleanScorer uses a ~16k array to score windows of
 * docs. So it scores docs 0-16k first, then docs 16-32k,
 * etc. For each window it iterates through all query terms
 * and accumulates a score in table[doc%16k]. It also stores
 * in the table a bitmask representing which terms
 * contributed to the score. Non-zero scores are chained in
 * a linked list. At the end of scoring each window it then
 * iterates through the linked list and, if the bitmask
 * matches the boolean constraints, collects a hit. For
 * boolean queries with lots of frequent terms this can be
 * much faster, since it does not need to update a priority
 * queue for each posting, instead performing constant-time
 * operations per posting. The only downside is that it
 * results in hits being delivered out-of-order within the
 * window, which means it cannot be nested within other
 * scorers. But it works well as a top-level scorer.
 *
 * The new BooleanScorer2 implementation instead works by
 * merging priority queues of postings, albeit with some
 * clever tricks. For example, a pure conjunction (all terms
 * required) does not require a priority queue. Instead it
 * sorts the posting streams at the start, then repeatedly
 * skips the first to to the last. If the first ever equals
 * the last, then there's a hit. When some terms are
 * required and some terms are optional, the conjunction can
 * be evaluated first, then the optional terms can all skip
 * to the match and be added to the score. Thus the
 * conjunction can reduce the number of priority queue
 * updates for the optional terms. */

final class CustomBooleanScorer extends CustomScorer {

  private static final class BooleanScorerCollector extends Collector {
    private BucketTable bucketTable;

    private int mask;

    private Scorer scorer;

    public BooleanScorerCollector(int mask, BucketTable bucketTable) {
      this.mask = mask;
      this.bucketTable = bucketTable;
    }

    @Override
    public final void collect(final int doc) throws IOException {
      final BucketTable table = this.bucketTable;
      final int i = doc & BucketTable.MASK;
      Bucket bucket = table.buckets[i];
      if (bucket == null)
        table.buckets[i] = bucket = new Bucket();

      if (bucket.doc != doc) { // invalid bucket
        bucket.doc = doc; // set doc
        bucket.score = this.scorer.score(); // initialize score
        bucket.bits = this.mask; // initialize mask
        bucket.coord = 1; // initialize coord

        bucket.next = table.first; // push onto valid list
        table.first = bucket;
      } else { // valid bucket
        bucket.score += this.scorer.score(); // increment score
        bucket.bits |= this.mask; // add bits in mask
        bucket.coord++; // increment coord
      }
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase) {
      // not needed by this implementation
    }

    @Override
    public void setScorer(Scorer scorer) throws IOException {
      this.scorer = scorer;
    }

    @Override
    public boolean acceptsDocsOutOfOrder() {
      return true;
    }

  }

  // An internal class which is used in score(Collector, int) for setting the
  // current score. This is required since Collector exposes a setScorer method
  // and implementations that need the score will call scorer.score().
  // Therefore the only methods that are implemented are score() and doc().
  private static final class BucketScorer extends CustomScorer {

    float score;

    int doc = NO_MORE_DOCS;

    public BucketScorer() {
      super(null);
    }

    @Override
    public int advance(int target) throws IOException {
      return NO_MORE_DOCS;
    }

    @Override
    public int docID() {
      return this.doc;
    }

    @Override
    public int nextDoc() throws IOException {
      return NO_MORE_DOCS;
    }

    @Override
    public float score() throws IOException {
      return this.score;
    }

    @Override
    public void matchedTermPositions(Map<String, MatchTermPositions> mtp, Set<String> scoreFields) {
      // TODO Auto-generated method stub

    }

  }

  static final class Bucket {
    int doc = -1; // tells if bucket is valid

    float score; // incremental score

    int bits; // used for bool constraints

    int coord; // count of terms in score

    Bucket next; // next valid bucket
  }

  /** A simple hash table of document scores within a range. */
  static final class BucketTable {
    public static final int SIZE = 1 << 11;

    public static final int MASK = SIZE - 1;

    final Bucket[] buckets = new Bucket[SIZE];

    Bucket first = null; // head of valid list

    public BucketTable() {}

    public Collector newCollector(int mask) {
      return new BooleanScorerCollector(mask, this);
    }

    public final int size() {
      return SIZE;
    }
  }

  static final class SubScorer {
    public Scorer scorer;

    public boolean required = false;

    public boolean prohibited = false;

    public Collector collector;

    public SubScorer next;

    public SubScorer(Scorer scorer, boolean required, boolean prohibited, Collector collector, SubScorer next)
        throws IOException {
      this.scorer = scorer;
      this.required = required;
      this.prohibited = prohibited;
      this.collector = collector;
      this.next = next;
    }

    public void matchedTermPositions(Map<String, MatchTermPositions> mtp, Set<String> scoreFields) {
      ((CustomScorer) this.scorer).matchedTermPositions(mtp, scoreFields);
      this.next.matchedTermPositions(mtp, scoreFields);

    }

  }

  private SubScorer scorers = null;

  private BucketTable bucketTable = new BucketTable();

  private int maxCoord = 1;

  private final float[] coordFactors;

  private int requiredMask = 0;

  private int prohibitedMask = 0;

  private int nextMask = 1;

  private final int minNrShouldMatch;

  private int end;

  private Bucket current;

  private int doc = -1;

  private float boost;

  CustomBooleanScorer(Similarity similarity, int minNrShouldMatch, List<CustomScorer> optionalScorers,
      List<CustomScorer> prohibitedScorers, float boost) throws IOException {
    super(similarity);
    this.boost = boost;
    this.minNrShouldMatch = minNrShouldMatch;

    if ((optionalScorers != null) && (optionalScorers.size() > 0)) {
      for (Scorer scorer : optionalScorers) {
        this.maxCoord++;
        if (scorer.nextDoc() != NO_MORE_DOCS) {
          this.scorers = new SubScorer(scorer, false, false, this.bucketTable.newCollector(0), this.scorers);
        }
      }
    }

    if ((prohibitedScorers != null) && (prohibitedScorers.size() > 0)) {
      for (Scorer scorer : prohibitedScorers) {
        int mask = this.nextMask;
        this.nextMask = this.nextMask << 1;
        this.prohibitedMask |= mask; // update prohibited mask
        if (scorer.nextDoc() != NO_MORE_DOCS) {
          this.scorers = new SubScorer(scorer, false, true, this.bucketTable.newCollector(mask), this.scorers);
        }
      }
    }

    this.coordFactors = new float[this.maxCoord];
    Similarity sim = getSimilarity();
    for (int i = 0; i < this.maxCoord; i++) {
      this.coordFactors[i] = sim.coord(i, this.maxCoord - 1);
    }
  }

  // firstDocID is ignored since nextDoc() initializes 'current'
  @Override
  protected boolean score(Collector collector, int max, int firstDocID) throws IOException {
    boolean more;
    Bucket tmp;
    BucketScorer bs = new BucketScorer();
    // The internal loop will set the score and doc before calling collect.
    collector.setScorer(bs);
    do {
      this.bucketTable.first = null;

      while (this.current != null) { // more queued

        // check prohibited & required
        if (((this.current.bits & this.prohibitedMask) == 0)
            && ((this.current.bits & this.requiredMask) == this.requiredMask)) {

          if (this.current.doc >= max) {
            tmp = this.current;
            this.current = this.current.next;
            tmp.next = this.bucketTable.first;
            this.bucketTable.first = tmp;
            continue;
          }

          if (this.current.coord >= this.minNrShouldMatch) {
            bs.score = this.current.score;

            bs.doc = this.current.doc;
            collector.collect(this.current.doc);
          }
        }

        this.current = this.current.next; // pop the queue
      }

      if (this.bucketTable.first != null) {
        this.current = this.bucketTable.first;
        this.bucketTable.first = this.current.next;
        return true;
      }

      // refill the queue
      more = false;
      this.end += BucketTable.SIZE;
      for (SubScorer sub = this.scorers; sub != null; sub = sub.next) {
        int subScorerDocID = sub.scorer.docID();
        if (subScorerDocID != NO_MORE_DOCS) {
          more |= sub.scorer.score(sub.collector, this.end, subScorerDocID);
        }
      }
      this.current = this.bucketTable.first;

    } while ((this.current != null) || more);

    return false;
  }

  @Override
  public int advance(int target) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int docID() {
    return this.doc;
  }

  @Override
  public int nextDoc() throws IOException {
    boolean more;
    do {
      while (this.bucketTable.first != null) { // more queued
        this.current = this.bucketTable.first;
        this.bucketTable.first = this.current.next; // pop the queue

        // check prohibited & required, and minNrShouldMatch
        if (((this.current.bits & this.prohibitedMask) == 0)
            && ((this.current.bits & this.requiredMask) == this.requiredMask)
            && (this.current.coord >= this.minNrShouldMatch)) {
          return this.doc = this.current.doc;
        }
      }

      // refill the queue
      more = false;
      this.end += BucketTable.SIZE;
      for (SubScorer sub = this.scorers; sub != null; sub = sub.next) {
        Scorer scorer = sub.scorer;
        sub.collector.setScorer(scorer);
        int doc = scorer.docID();
        while (doc < this.end) {
          sub.collector.collect(doc);
          doc = scorer.nextDoc();
        }
        more |= (doc != NO_MORE_DOCS);
      }
    } while ((this.bucketTable.first != null) || more);

    return this.doc = NO_MORE_DOCS;
  }

  @Override
  public float score() {
    return this.current.score + this.boost;
  }

  @Override
  public void score(Collector collector) throws IOException {
    score(collector, Integer.MAX_VALUE, nextDoc());
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    buffer.append("boolean(");
    for (SubScorer sub = this.scorers; sub != null; sub = sub.next) {
      buffer.append(sub.scorer.toString());
      buffer.append(" ");
    }
    buffer.append(")");
    return buffer.toString();
  }

  @Override
  public void matchedTermPositions(Map<String, MatchTermPositions> mtp, Set<String> scoreFields) {
    this.scorers.matchedTermPositions(mtp, scoreFields);

  }

}
