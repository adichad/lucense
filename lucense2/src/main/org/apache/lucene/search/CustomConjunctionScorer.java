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
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermPositions;

/** Scorer for conjunctions, sets of queries, all of which are required. */
class CustomConjunctionScorer extends CustomScorer {

  private final Scorer[] scorers;

  // private final float coord;
  private int lastDoc = -1;

  private Map<String, Integer> currentNumwords;

  private Map<Term, Set<Integer>> currentQueryPosMap;

  private Map<Term, Set<TermPositions>> currentDocPosMap;

  public CustomConjunctionScorer(Similarity similarity, Collection<Scorer> scorers) throws IOException {
    this(similarity, scorers.toArray(new Scorer[scorers.size()]));
  }

  public CustomConjunctionScorer(Similarity similarity, Scorer[] scorers) throws IOException {
    super(similarity);
    this.scorers = scorers;
    // coord = similarity.coord(scorers.length, scorers.length);

    for (int i = 0; i < scorers.length; i++) {
      if (scorers[i].nextDoc() == NO_MORE_DOCS) {
        // If even one of the sub-scorers does not have any documents, this
        // scorer should not attempt to do any more work.
        this.lastDoc = NO_MORE_DOCS;
        return;
      }
    }

    // Sort the array the first time...
    // We don't need to sort the array in any future calls because we know
    // it will already start off sorted (all scorers on same doc).

    // note that this comparator is not consistent with equals!
    Arrays.sort(scorers, new Comparator<Scorer>() { // sort the array
          @Override
          public int compare(Scorer o1, Scorer o2) {
            return o1.docID() - o2.docID();
          }
        });

    // NOTE: doNext() must be called before the re-sorting of the array later
    // on.
    // The reason is this: assume there are 5 scorers, whose first docs are 1,
    // 2, 3, 5, 5 respectively. Sorting (above) leaves the array as is. Calling
    // doNext() here advances all the first scorers to 5 (or a larger doc ID
    // they all agree on).
    // However, if we re-sort before doNext() is called, the order will be 5, 3,
    // 2, 1, 5 and then doNext() will stop immediately, since the first scorer's
    // docs equals the last one. So the invariant that after calling doNext()
    // all scorers are on the same doc ID is broken.
    if (doNext() == NO_MORE_DOCS) {
      // The scorers did not agree on any document.
      this.lastDoc = NO_MORE_DOCS;
      return;
    }

    // If first-time skip distance is any predictor of
    // scorer sparseness, then we should always try to skip first on
    // those scorers.
    // Keep last scorer in it's last place (it will be the first
    // to be skipped on), but reverse all of the others so that
    // they will be skipped on in order of original high skip.
    int end = scorers.length - 1;
    int max = end >> 1;
    for (int i = 0; i < max; i++) {
      Scorer tmp = scorers[i];
      int idx = end - i - 1;
      scorers[i] = scorers[idx];
      scorers[idx] = tmp;
    }
  }

  private int doNext() throws IOException {
    int first = 0;
    int doc = this.scorers[this.scorers.length - 1].docID();
    Scorer firstScorer;
    while ((firstScorer = this.scorers[first]).docID() < doc) {
      doc = firstScorer.advance(doc);
      first = first == this.scorers.length - 1 ? 0 : first + 1;
    }
    return doc;
  }

  @Override
  public int advance(int target) throws IOException {
    if (this.lastDoc == NO_MORE_DOCS) {
      return this.lastDoc;
    } else if (this.scorers[(this.scorers.length - 1)].docID() < target) {
      this.scorers[(this.scorers.length - 1)].advance(target);
    }
    return this.lastDoc = doNext();
  }

  /** @deprecated use {@link #docID()} instead. */
  @Deprecated
  public int doc() {
    return this.lastDoc;
  }

  @Override
  public int docID() {
    return this.lastDoc;
  }

  public Explanation explain(int doc) {
    throw new UnsupportedOperationException();
  }

  /** @deprecated use {@link #nextDoc()} instead. */
  @Deprecated
  public boolean next() throws IOException {
    return nextDoc() != NO_MORE_DOCS;
  }

  @Override
  public int nextDoc() throws IOException {
    if (this.lastDoc == NO_MORE_DOCS) {
      return this.lastDoc;
    } else if (this.lastDoc == -1) {
      return this.lastDoc = this.scorers[this.scorers.length - 1].docID();
    }
    this.scorers[(this.scorers.length - 1)].nextDoc();
    return this.lastDoc = doNext();
  }

  @Override
  public float score() throws IOException {
    float sum = 0.0f;
    for (int i = 0; i < this.scorers.length; i++) {
      sum += this.scorers[i].score();
    }
    return sum;
  }

  /** @deprecated use {@link #advance(int)} instead. */
  @Deprecated
  public boolean skipTo(int target) throws IOException {
    return advance(target) != NO_MORE_DOCS;
  }

  @Override
  public void matchedTermPositions(Map<String, MatchTermPositions> mtp, Set<String> scoreFields) throws IOException {
    for (int i = 0; i < this.scorers.length; i++) {
      if(this.scorers[i] instanceof CustomScorer)
        ((CustomScorer) this.scorers[i]).matchedTermPositions(mtp, scoreFields);
    }

  }

}
