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

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermPositions;

final class CustomExactPhraseScorer extends CustomPhraseScorer {

  CustomExactPhraseScorer(Weight weight, TermPositions[] tps, int[] offsets, Similarity similarity, byte[] norms,
      String field, ArrayList<Term> terms, int[] qpositions, TermPositions[] tps2) {
    super(weight, tps, offsets, similarity, norms, field, terms, qpositions, tps2);
  }

  @Override
  protected final float phraseFreq() throws IOException {
    // sort list with pq
    this.pq.clear();
    for (PhrasePositions pp = this.first; pp != null; pp = pp.next) {
      pp.firstPosition();
      this.pq.add(pp); // build pq from list
    }
    pqToList(); // rebuild list from pq

    // for counting how many times the exact phrase is found in current
    // document,
    // just count how many times all PhrasePosition's have exactly the same
    // position.
    int freq = 0;
    do { // find position w/ all terms
      while (this.first.position < this.last.position) { // scan forward in
                                                         // first
        do {
          if (!this.first.nextPosition())
            return freq;
        } while (this.first.position < this.last.position);
        firstToLast();
      }
      freq++; // all equal: a match
    } while (this.last.nextPosition());

    return freq;
  }
}
