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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/* See the description in BooleanScorer.java, comparing
 * BooleanScorer & CustomBooleanScorer2 */

/**
 * An alternative to BooleanScorer that also allows a minimum number of optional
 * scorers that should match. <br>
 * Implements skipTo(), and has no limitations on the numbers of added scorers. <br>
 * Uses ConjunctionScorer, DisjunctionScorer, ReqOptScorer and ReqExclScorer.
 */
class CustomBooleanScorer2 extends CustomScorer {

  private final List<CustomScorer> requiredScorers;

  private final List<CustomScorer> optionalScorers;

  private final List<CustomScorer> prohibitedScorers;

  private class Coordinator {
    float[] coordFactors = null;

    int maxCoord = 0; // to be increased for each non prohibited
                      // scorer

    int nrMatchers; // to be increased by score() of match counting
                    // scorers.

    void init() { // use after all scorers have been added.
      this.coordFactors = new float[this.maxCoord + 1];
      Similarity sim = getSimilarity();
      for (int i = 0; i <= this.maxCoord; i++) {
        this.coordFactors[i] = sim.coord(i, this.maxCoord);
      }
    }

  }

  private final Coordinator coordinator;

  /**
   * The scorer to which all scoring will be delegated, except for computing and
   * using the coordination factor.
   */
  private final Scorer countingSumScorer;

  /** The number of optionalScorers that need to match (if there are any) */
  private final int minNrShouldMatch;

  private int doc = -1;

  private float boost;

  /**
   * Creates a {@link Scorer} with the given similarity and lists of required,
   * prohibited and optional scorers. In no required scorers are added, at least
   * one of the optional scorers will have to match during the search.
   * 
   * @param similarity
   *          The similarity to be used.
   * @param minNrShouldMatch
   *          The minimum number of optional added scorers that should match
   *          during the search. In case no required scorers are added, at least
   *          one of the optional scorers will have to match during the search.
   * @param required
   *          the list of required scorers.
   * @param prohibited
   *          the list of prohibited scorers.
   * @param optional
   *          the list of optional scorers.
   */
  public CustomBooleanScorer2(Similarity similarity, int minNrShouldMatch, List<CustomScorer> required,
      List<CustomScorer> prohibited, List<CustomScorer> optional, float boost) throws IOException {
    super(similarity);
    this.boost = boost;
    if (minNrShouldMatch < 0) {
      throw new IllegalArgumentException("Minimum number of optional scorers should not be negative");
    }
    this.coordinator = new Coordinator();
    this.minNrShouldMatch = minNrShouldMatch;

    this.optionalScorers = optional;
    this.coordinator.maxCoord += optional.size();

    this.requiredScorers = required;
    this.coordinator.maxCoord += required.size();

    this.prohibitedScorers = prohibited;

    this.coordinator.init();
    this.countingSumScorer = makeCountingSumScorer();
  }

  /** Count a scorer as a single match. */
  private class CustomSingleMatchScorer extends CustomScorer {
    private Scorer scorer;

    private int lastScoredDoc = -1;

    // Save the score of lastScoredDoc, so that we don't compute it more than
    // once in score().
    private float lastDocScore = Float.NaN;

    CustomSingleMatchScorer(Scorer scorer) {
      super(scorer.getSimilarity());
      this.scorer = scorer;
    }

    @Override
    public float score() throws IOException {
      int doc = docID();
      if (doc >= this.lastScoredDoc) {
        if (doc > this.lastScoredDoc) {
          this.lastDocScore = this.scorer.score();
          this.lastScoredDoc = doc;
        }
        CustomBooleanScorer2.this.coordinator.nrMatchers++;
      }
      return this.lastDocScore;
    }

    @Override
    public int docID() {
      return this.scorer.docID();
    }

    @Override
    public int nextDoc() throws IOException {
      return this.scorer.nextDoc();
    }

    @Override
    public int advance(int target) throws IOException {
      return this.scorer.advance(target);
    }

    @Override
    public void matchedTermPositions(Map<String, MatchTermPositions> mtp, Set<String> scoreFields) {
      ((CustomScorer) this.scorer).matchedTermPositions(mtp, scoreFields);

    }

  }

  private CustomScorer countingDisjunctionSumScorer(final List<CustomScorer> scorers, int minNrShouldMatch)
      throws IOException {
    // each scorer from the list counted as a single matcher
    return new CustomDisjunctionSumScorer(scorers, minNrShouldMatch) {
      private int lastScoredDoc = -1;

      // Save the score of lastScoredDoc, so that we don't compute it more than
      // once in score().
      private float lastDocScore = Float.NaN;

      @Override
      public float score() throws IOException {
        int doc = docID();
        if (doc >= this.lastScoredDoc) {
          if (doc > this.lastScoredDoc) {
            this.lastDocScore = super.score();
            this.lastScoredDoc = doc;
          }
          CustomBooleanScorer2.this.coordinator.nrMatchers += super.nrMatchers;
        }
        return this.lastDocScore;
      }

    };
  }

  private static final Similarity defaultSimilarity = Similarity.getDefault();

  private CustomScorer countingConjunctionSumScorer(List<CustomScorer> requiredScorers) throws IOException {
    // each scorer from the list counted as a single matcher
    final int requiredNrMatchers = requiredScorers.size();
    return new CustomConjunctionScorer(defaultSimilarity, requiredScorers) {
      private int lastScoredDoc = -1;

      // Save the score of lastScoredDoc, so that we don't compute it more than
      // once in score().
      private float lastDocScore = Float.NaN;

      @Override
      public float score() throws IOException {
        int doc = docID();
        if (doc >= this.lastScoredDoc) {
          if (doc > this.lastScoredDoc) {
            this.lastDocScore = super.score();
            this.lastScoredDoc = doc;
          }
          CustomBooleanScorer2.this.coordinator.nrMatchers += requiredNrMatchers;
        }
        // All scorers match, so defaultSimilarity super.score() always has 1 as
        // the coordination factor.
        // Therefore the sum of the scores of the requiredScorers
        // is used as score.
        return this.lastDocScore;
      }

    };
  }

  private CustomScorer dualConjunctionSumScorer(Scorer req1, Scorer req2) throws IOException { // non
                                                                                               // counting.
    return new CustomConjunctionScorer(defaultSimilarity, new Scorer[] { req1, req2 });
    // All scorers match, so defaultSimilarity always has 1 as
    // the coordination factor.
    // Therefore the sum of the scores of two scorers
    // is used as score.
  }

  /**
   * Returns the scorer to be used for match counting and score summing. Uses
   * requiredScorers, optionalScorers and prohibitedScorers.
   */
  private CustomScorer makeCountingSumScorer() throws IOException { // each
                                                                    // scorer
                                                                    // counted
                                                                    // as a
                                                                    // single
                                                                    // matcher
    return (this.requiredScorers.size() == 0) ? makeCountingSumScorerNoReq() : makeCountingSumScorerSomeReq();
  }

  private CustomScorer makeCountingSumScorerNoReq() throws IOException { // No
                                                                         // required
                                                                         // scorers
    // minNrShouldMatch optional scorers are required, but at least 1
    int nrOptRequired = (this.minNrShouldMatch < 1) ? 1 : this.minNrShouldMatch;
    CustomScorer requiredCountingSumScorer;
    if (this.optionalScorers.size() > nrOptRequired)
      requiredCountingSumScorer = countingDisjunctionSumScorer(this.optionalScorers, nrOptRequired);
    else if (this.optionalScorers.size() == 1)
      requiredCountingSumScorer = new CustomSingleMatchScorer(this.optionalScorers.get(0));
    else
      requiredCountingSumScorer = countingConjunctionSumScorer(this.optionalScorers);
    return addProhibitedScorers(requiredCountingSumScorer);
  }

  private CustomScorer makeCountingSumScorerSomeReq() throws IOException { // At
                                                                           // least
                                                                           // one
                                                                           // required
                                                                           // scorer.
    if (this.optionalScorers.size() == this.minNrShouldMatch) { // all optional
                                                                // scorers
      // also required.
      ArrayList<CustomScorer> allReq = new ArrayList<CustomScorer>(this.requiredScorers);
      allReq.addAll(this.optionalScorers);
      return addProhibitedScorers(countingConjunctionSumScorer(allReq));
    } else { // optionalScorers.size() > minNrShouldMatch, and at least one
             // required scorer
      CustomScorer requiredCountingSumScorer = this.requiredScorers.size() == 1 ? new CustomSingleMatchScorer(
          this.requiredScorers.get(0)) : countingConjunctionSumScorer(this.requiredScorers);
      if (this.minNrShouldMatch > 0) { // use a required disjunction scorer over
                                       // the
        // optional scorers
        return addProhibitedScorers(dualConjunctionSumScorer( // non counting
            requiredCountingSumScorer, countingDisjunctionSumScorer(this.optionalScorers, this.minNrShouldMatch)));
      } else { // minNrShouldMatch == 0
        return new CustomReqOptSumScorer(addProhibitedScorers(requiredCountingSumScorer),
            this.optionalScorers.size() == 1 ? new CustomSingleMatchScorer(this.optionalScorers.get(0))
            // require 1 in combined, optional scorer.
                : countingDisjunctionSumScorer(this.optionalScorers, 1));
      }
    }
  }

  /**
   * Returns the scorer to be used for match counting and score summing. Uses
   * the given required scorer and the prohibitedScorers.
   * 
   * @param requiredCountingSumScorer
   *          A required scorer already built.
   */
  private CustomScorer addProhibitedScorers(CustomScorer requiredCountingSumScorer) throws IOException {
    return (this.prohibitedScorers.size() == 0) ? requiredCountingSumScorer // no
        // prohibited
        : new CustomReqExclScorer(requiredCountingSumScorer,
            ((this.prohibitedScorers.size() == 1) ? this.prohibitedScorers.get(0) : new CustomDisjunctionSumScorer(
                this.prohibitedScorers)));
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
    while ((this.doc = this.countingSumScorer.nextDoc()) != NO_MORE_DOCS) {
      // System.out.println("collector called for doc: "+doc);
      collector.collect(this.doc);
    }
  }

  @Override
  protected boolean score(Collector collector, int max, int firstDocID) throws IOException {
    this.doc = firstDocID;
    collector.setScorer(this);
    while (this.doc < max) {
      collector.collect(this.doc);
      this.doc = this.countingSumScorer.nextDoc();
    }
    return this.doc != NO_MORE_DOCS;
  }

  @Override
  public int docID() {
    return this.doc;
  }

  @Override
  public int nextDoc() throws IOException {
    return this.doc = this.countingSumScorer.nextDoc();
  }

  @Override
  public float score() throws IOException {
    this.coordinator.nrMatchers = 0;
    float sum = this.countingSumScorer.score();

    return sum + this.boost;
  }

  /** @deprecated use {@link #advance(int)} instead. */
  @Deprecated
  public boolean skipTo(int target) throws IOException {
    return advance(target) != NO_MORE_DOCS;
  }

  @Override
  public int advance(int target) throws IOException {
    return this.doc = this.countingSumScorer.advance(target);
  }

  /**
   * Throws an UnsupportedOperationException. TODO: Implement an explanation of
   * the coordination factor.
   * 
   * @param doc
   *          The document number for the explanation.
   * @throws UnsupportedOperationException
   */
  public Explanation explain(int doc) {
    throw new UnsupportedOperationException();
    /*
     * How to explain the coordination factor? initCountingSumScorer(); return
     * countingSumScorer.explain(doc); // misses coord factor.
     */
  }

  @Override
  public void matchedTermPositions(Map<String, MatchTermPositions> mtp, Set<String> scoreFields) {
    for (CustomScorer scorer : this.requiredScorers) {
      scorer.matchedTermPositions(mtp, scoreFields);
    }
    for (CustomScorer scorer : this.optionalScorers) {
      scorer.matchedTermPositions(mtp, scoreFields);
    }
    for (CustomScorer scorer : this.prohibitedScorers) {
      scorer.matchedTermPositions(mtp, scoreFields);
    }

  }
}
