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
import java.util.Map;
import java.util.Set;

/**
 * A Scorer for queries with a required part and an optional part. Delays
 * skipTo() on the optional part until a score() is needed. <br>
 * This <code>Scorer</code> implements {@link Scorer#skipTo(int)}.
 */
class CustomReqOptSumScorer extends CustomScorer {
  /**
   * The scorers passed from the constructor. These are set to null as soon as
   * their next() or skipTo() returns false.
   */
  private CustomScorer reqScorer;

  private CustomScorer optScorer;

  @Override
  public void matchedTermPositions(Map<String, MatchTermPositions> mtp, Set<String> scoreFields) throws IOException {
    this.reqScorer.matchedTermPositions(mtp, scoreFields);
    this.optScorer.matchedTermPositions(mtp, scoreFields);
  }

  /**
   * Construct a <code>ReqOptScorer</code>.
   * 
   * @param reqScorer
   *          The required scorer. This must match.
   * @param optScorer
   *          The optional scorer. This is used for scoring only.
   */
  public CustomReqOptSumScorer(CustomScorer reqScorer, CustomScorer optScorer) {
    super(null); // No similarity used.
    this.reqScorer = reqScorer;
    this.optScorer = optScorer;
  }

  @Override
  public int nextDoc() throws IOException {
    return this.reqScorer.nextDoc();
  }

  @Override
  public int advance(int target) throws IOException {
    return this.reqScorer.advance(target);
  }

  @Override
  public int docID() {
    return this.reqScorer.docID();
  }

  /**
   * Returns the score of the current document matching the query. Initially
   * invalid, until {@link #next()} is called the first time.
   * 
   * @return The score of the required scorer, eventually increased by the score
   *         of the optional scorer when it also matches the current document.
   */
  @Override
  public float score() throws IOException {
    int curDoc = this.reqScorer.docID();
    float reqScore = this.reqScorer.score();
    if (this.optScorer == null) {
      return reqScore;
    }

    int optScorerDoc = this.optScorer.docID();
    if ((optScorerDoc < curDoc) && ((optScorerDoc = this.optScorer.advance(curDoc)) == NO_MORE_DOCS)) {
      this.optScorer = null;
      return reqScore;
    }
    if (optScorerDoc == curDoc) {
      return reqScore + this.optScorer.score();
    }
    return reqScore;
  }
}
