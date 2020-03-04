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
 * A {@link Scorer} which wraps another scorer and caches the score of the
 * current document. Successive calls to {@link #score()} will return the same
 * result and will not invoke the wrapped Scorer's score() method, unless the
 * current document has changed.<br>
 * This class might be useful due to the changes done to the {@link Collector}
 * interface, in which the score is not computed for a document by default, only
 * if the collector requests it. Some collectors may need to use the score in
 * several places, however all they have in hand is a {@link Scorer} object, and
 * might end up computing the score of a document more than once.
 */
public class CustomScoreCachingWrappingScorer extends CustomScorer {

  private Scorer scorer;

  private int curDoc = -1;

  private float curScore;

  @Override
  public void matchedTermPositions(Map<String, MatchTermPositions> mtp, Set<String> scoreFields) {
    ((CustomScorer) this.scorer).matchedTermPositions(mtp, scoreFields);
  }

  /** Creates a new instance by wrapping the given scorer. */
  public CustomScoreCachingWrappingScorer(Scorer scorer) {
    super(scorer.getSimilarity());
    this.scorer = scorer;
  }

  @Override
  protected boolean score(Collector collector, int max, int firstDocID) throws IOException {
    return this.scorer.score(collector, max, firstDocID);
  }

  @Override
  public Similarity getSimilarity() {
    return this.scorer.getSimilarity();
  }

  @Override
  public float score() throws IOException {
    int doc = this.scorer.docID();
    if (doc != this.curDoc) {
      this.curScore = this.scorer.score();
      this.curDoc = doc;
    }
    return this.curScore;
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
  public void score(Collector collector) throws IOException {
    this.scorer.score(collector);
  }

  @Override
  public int advance(int target) throws IOException {
    return this.scorer.advance(target);
  }

}
