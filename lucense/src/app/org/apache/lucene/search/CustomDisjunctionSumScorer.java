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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.util.ScorerDocQueue;

/**
 * A Scorer for OR like queries, counterpart of <code>ConjunctionScorer</code>.
 * This Scorer implements {@link Scorer#skipTo(int)} and uses skipTo() on the
 * given Scorers. TODO: Implement score(HitCollector, int).
 */
class CustomDisjunctionSumScorer extends CustomScorer {
  /** The number of subscorers. */
  private final int nrScorers;

  /** The subscorers. */
  protected final List<CustomScorer> subScorers;

  @Override
  public void matchedTermPositions(Map<String, MatchTermPositions> mtp, Set<String> scoreFields) {
    for (CustomScorer scorer : this.subScorers) {
      scorer.matchedTermPositions(mtp, scoreFields);
    }
  }

  /** The minimum number of scorers that should match. */
  private final int minimumNrMatchers;

  /**
   * The scorerDocQueue contains all subscorers ordered by their current doc(),
   * with the minimum at the top. <br>
   * The scorerDocQueue is initialized the first time next() or skipTo() is
   * called. <br>
   * An exhausted scorer is immediately removed from the scorerDocQueue. <br>
   * If less than the minimumNrMatchers scorers remain in the scorerDocQueue
   * next() and skipTo() return false.
   * <p>
   * After each to call to next() or skipTo() <code>currentSumScore</code> is
   * the total score of the current matching doc, <code>nrMatchers</code> is the
   * number of matching scorers, and all scorers are after the matching doc, or
   * are exhausted.
   */
  private ScorerDocQueue scorerDocQueue;

  /** The document number of the current match. */
  private int currentDoc = -1;

  /** The number of subscorers that provide the current match. */
  protected int nrMatchers = -1;

  private float currentScore = Float.NaN;

  /**
   * Construct a <code>DisjunctionScorer</code>.
   * 
   * @param subScorers
   *          A collection of at least two subscorers.
   * @param minimumNrMatchers
   *          The positive minimum number of subscorers that should match to
   *          match this query. <br>
   *          When <code>minimumNrMatchers</code> is bigger than the number of
   *          <code>subScorers</code>, no matches will be produced. <br>
   *          When minimumNrMatchers equals the number of subScorers, it more
   *          efficient to use <code>ConjunctionScorer</code>.
   */
  public CustomDisjunctionSumScorer(List<CustomScorer> subScorers, int minimumNrMatchers) throws IOException {
    super(null);

    this.nrScorers = subScorers.size();

    if (minimumNrMatchers <= 0) {
      throw new IllegalArgumentException("Minimum nr of matchers must be positive");
    }
    if (this.nrScorers <= 1) {
      throw new IllegalArgumentException("There must be at least 2 subScorers");
    }

    this.minimumNrMatchers = minimumNrMatchers;
    this.subScorers = subScorers;

    initScorerDocQueue();
  }

  /**
   * Construct a <code>DisjunctionScorer</code>, using one as the minimum number
   * of matching subscorers.
   */
  public CustomDisjunctionSumScorer(List<CustomScorer> subScorers) throws IOException {
    this(subScorers, 1);
  }

  /**
   * Called the first time next() or skipTo() is called to initialize
   * <code>scorerDocQueue</code>.
   */
  private void initScorerDocQueue() throws IOException {
    Iterator<CustomScorer> si = this.subScorers.iterator();
    this.scorerDocQueue = new ScorerDocQueue(this.nrScorers);
    while (si.hasNext()) {
      Scorer se = si.next();
      if (se.nextDoc() != NO_MORE_DOCS) { // doc() method will be used in
                                          // scorerDocQueue.
        this.scorerDocQueue.insert(se);
      }
    }
  }

  /**
   * Scores and collects all matching documents.
   * 
   * @param collector
   *          The collector to which all matching documents are passed through. <br>
   *          When this method is used the {@link #explain(int)} method should
   *          not be used.
   */
  @Override
  public void score(Collector collector) throws IOException {
    collector.setScorer(this);
    while (nextDoc() != NO_MORE_DOCS) {
      collector.collect(this.currentDoc);
    }
  }

  /**
   * Expert: Collects matching documents in a range. Hook for optimization. Note
   * that {@link #next()} must be called once before this method is called for
   * the first time.
   * 
   * @param collector
   *          The collector to which all matching documents are passed through.
   * @param max
   *          Do not score documents past this.
   * @return true if more matching documents may remain.
   */
  @Override
  protected boolean score(Collector collector, int max, int firstDocID) throws IOException {
    // firstDocID is ignored since nextDoc() sets 'currentDoc'
    collector.setScorer(this);
    while (this.currentDoc < max) {
      collector.collect(this.currentDoc);
      if (nextDoc() == NO_MORE_DOCS) {
        return false;
      }
    }
    return true;
  }

  /** @deprecated use {@link #nextDoc()} instead. */
  @Deprecated
  public boolean next() throws IOException {
    return nextDoc() != NO_MORE_DOCS;
  }

  @Override
  public int nextDoc() throws IOException {
    if ((this.scorerDocQueue.size() < this.minimumNrMatchers) || !advanceAfterCurrent()) {
      this.currentDoc = NO_MORE_DOCS;
    }
    return this.currentDoc;
  }

  /**
   * Advance all subscorers after the current document determined by the top of
   * the <code>scorerDocQueue</code>. Repeat until at least the minimum number
   * of subscorers match on the same document and all subscorers are after that
   * document or are exhausted. <br>
   * On entry the <code>scorerDocQueue</code> has at least
   * <code>minimumNrMatchers</code> available. At least the scorer with the
   * minimum document number will be advanced.
   * 
   * @return true iff there is a match. <br>
   *         In case there is a match, </code>currentDoc</code>,
   *         </code>currentSumScore</code>, and </code>nrMatchers</code>
   *         describe the match.
   * 
   *         TODO: Investigate whether it is possible to use skipTo() when the
   *         minimum number of matchers is bigger than one, ie. try and use the
   *         character of ConjunctionScorer for the minimum number of matchers.
   *         Also delay calling score() on the sub scorers until the minimum
   *         number of matchers is reached. <br>
   *         For this, a Scorer array with minimumNrMatchers elements might hold
   *         Scorers at currentDoc that are temporarily popped from scorerQueue.
   */
  protected boolean advanceAfterCurrent() throws IOException {
    do { // repeat until minimum nr of matchers
      this.currentDoc = this.scorerDocQueue.topDoc();
      this.currentScore = this.scorerDocQueue.topScore();

      this.nrMatchers = 1;
      do { // Until all subscorers are after currentDoc
        if (!this.scorerDocQueue.topNextAndAdjustElsePop()) {
          if (this.scorerDocQueue.size() == 0) {
            break; // nothing more to advance, check for last match.
          }
        }
        if (this.scorerDocQueue.topDoc() != this.currentDoc) {
          break; // All remaining subscorers are after currentDoc.
        }
        this.currentScore += this.scorerDocQueue.topScore();
        this.nrMatchers++;
      } while (true);

      if (this.nrMatchers >= this.minimumNrMatchers) {
        return true;
      } else if (this.scorerDocQueue.size() < this.minimumNrMatchers) {
        return false;
      }
    } while (true);
  }

  /**
   * Returns the score of the current document matching the query. Initially
   * invalid, until {@link #next()} is called the first time.
   */
  @Override
  public float score() throws IOException {
    return this.currentScore;
  }

  /** @deprecated use {@link #docID()} instead. */
  @Deprecated
  public int doc() {
    return this.currentDoc;
  }

  @Override
  public int docID() {
    return this.currentDoc;
  }

  /**
   * Returns the number of subscorers matching the current document. Initially
   * invalid, until {@link #next()} is called the first time.
   */
  public int nrMatchers() {
    return this.nrMatchers;
  }

  /**
   * Skips to the first match beyond the current whose document number is
   * greater than or equal to a given target. <br>
   * When this method is used the {@link #explain(int)} method should not be
   * used. <br>
   * The implementation uses the skipTo() method on the subscorers.
   * 
   * @param target
   *          The target document number.
   * @return true iff there is such a match.
   * @deprecated use {@link #advance(int)} instead.
   */
  @Deprecated
  public boolean skipTo(int target) throws IOException {
    return advance(target) != NO_MORE_DOCS;
  }

  /**
   * Advances to the first match beyond the current whose document number is
   * greater than or equal to a given target. <br>
   * When this method is used the {@link #explain(int)} method should not be
   * used. <br>
   * The implementation uses the skipTo() method on the subscorers.
   * 
   * @param target
   *          The target document number.
   * @return the document whose number is greater than or equal to the given
   *         target, or -1 if none exist.
   */
  @Override
  public int advance(int target) throws IOException {
    if (this.scorerDocQueue.size() < this.minimumNrMatchers) {
      return this.currentDoc = NO_MORE_DOCS;
    }
    if (target <= this.currentDoc) {
      return this.currentDoc;
    }
    do {
      if (this.scorerDocQueue.topDoc() >= target) {
        return advanceAfterCurrent() ? this.currentDoc : (this.currentDoc = NO_MORE_DOCS);
      } else if (!this.scorerDocQueue.topSkipToAndAdjustElsePop(target)) {
        if (this.scorerDocQueue.size() < this.minimumNrMatchers) {
          return this.currentDoc = NO_MORE_DOCS;
        }
      }
    } while (true);
  }

}
