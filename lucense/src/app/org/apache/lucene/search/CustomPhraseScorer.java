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
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermPositions;

/**
 * Expert: Scoring functionality for phrase queries. <br>
 * A document is considered matching if it contains the phrase-query terms at
 * "valid" positions. What "valid positions" are depends on the type of the
 * phrase query: for an exact phrase query terms are required to appear in
 * adjacent locations, while for a sloppy phrase query some distance between the
 * terms is allowed. The abstract method {@link #phraseFreq()} of extending
 * classes is invoked for each document containing all the phrase query terms,
 * in order to compute the frequency of the phrase query in that document. A non
 * zero frequency means a match.
 */
abstract class CustomPhraseScorer extends CustomScorer {
  private Weight weight;

  protected byte[] norms;

  protected float value;

  private boolean firstTime = true;

  private boolean more = true;

  protected PhraseQueue pq;

  protected PhrasePositions first, last;

  private float freq; // phrase

  private TermPositions[] tps;

  // frequency in
  // current doc
  // as computed
  // by
  // phraseFreq().
  private ArrayList<Term> terms;

  private int[] qpositions;

  private TermPositions[] tps2;

  CustomPhraseScorer(Weight weight, TermPositions[] tps, int[] offsets, Similarity similarity, byte[] norms,
      String field, ArrayList<Term> terms, int[] qpositions, TermPositions[] tps2) {
    super(similarity);
    this.norms = norms;
    this.weight = weight;
    this.value = weight.getQuery().getBoost();
    this.tps = tps;
    this.terms = terms;
    this.qpositions = qpositions;
    this.tps2 = tps2;

    // convert tps to a list of phrase positions.
    // note: phrase-position differs from term-position in that its position
    // reflects the phrase offset: pp.pos = tp.pos - offset.
    // this allows to easily identify a matching (exact) phrase
    // when all PhrasePositions have exactly the same position.
    for (int i = 0; i < tps.length; i++) {
      PhrasePositions pp = new PhrasePositions(tps[i], offsets[i], i);
      if (this.last != null) { // add next to end of list
        this.last.next = pp;
      } else {
        this.first = pp;
      }
      this.last = pp;
    }

    this.pq = new PhraseQueue(tps.length); // construct empty pq
    this.first.doc = -1;
  }

  @Override
  public void matchedTermPositions(Map<String, MatchTermPositions> mtp, Set<String> scoreFields) {

    for (int i = 0; i < this.terms.size(); i++) {
      String field = this.terms.get(i).field();
      if (scoreFields.contains(field)) {
        MatchTermPositions pos;
        if (!mtp.containsKey(field)) {
          pos = new MatchTermPositions();
          mtp.put(field, pos);
        } else {
          pos = mtp.get(field);
        }
        pos.add(this.qpositions[i], this.tps2[i], this.terms.get(i));
      }
    }
  }

  @Override
  public int docID() {
    return this.first.doc;
  }

  @Override
  public int nextDoc() throws IOException {
    if (this.firstTime) {
      init();
      this.firstTime = false;
    } else if (this.more) {
      this.more = this.last.next(); // trigger further scanning
    }
    if (!doNext()) {
      this.first.doc = NO_MORE_DOCS;
    }
    return this.first.doc;
  }

  // next without initial increment
  private boolean doNext() throws IOException {
    while (this.more) {
      while (this.more && (this.first.doc < this.last.doc)) { // find doc w/ all
                                                              // the terms
        this.more = this.first.skipTo(this.last.doc); // skip first upto last

        firstToLast(); // and move it to the end
      }

      if (this.more) {
        // found a doc with all of the terms
        this.freq = phraseFreq(); // check for phrase
        if (this.freq == 0.0f) { // no match
          this.more = this.last.next(); // trigger further scanning

        } else
          return true; // found a match
      }
    }
    return false; // no more matches
  }

  @Override
  public float score() throws IOException {
    // System.out.println("scoring " + first.doc);
    return this.value;
  }

  @Override
  public int advance(int target) throws IOException {
    this.firstTime = false;

    for (PhrasePositions pp = this.first; this.more && (pp != null); pp = pp.next) {
      this.more = pp.skipTo(target);
    }
    if (this.more) {
      sort(); // re-sort
    }
    if (!doNext()) {
      this.first.doc = NO_MORE_DOCS;
    }
    return this.first.doc;
  }

  /**
   * For a document containing all the phrase query terms, compute the frequency
   * of the phrase in that document. A non zero frequency means a match. <br>
   * Note, that containing all phrase terms does not guarantee a match - they
   * have to be found in matching locations.
   * 
   * @return frequency of the phrase in current doc, 0 if not found.
   */
  protected abstract float phraseFreq() throws IOException;

  private void init() throws IOException {
    for (PhrasePositions pp = this.first; this.more && (pp != null); pp = pp.next) {
      this.more = pp.next();
    }
    if (this.more) {
      sort();
    }
  }

  private void sort() {
    this.pq.clear();
    for (PhrasePositions pp = this.first; pp != null; pp = pp.next) {
      this.pq.add(pp);
    }
    pqToList();
  }

  protected final void pqToList() {
    this.last = this.first = null;
    while (this.pq.top() != null) {
      PhrasePositions pp = this.pq.pop();
      if (this.last != null) { // add next to end of list
        this.last.next = pp;
      } else
        this.first = pp;
      this.last = pp;
      pp.next = null;
    }
  }

  protected final void firstToLast() {
    this.last.next = this.first; // move first to end of list
    this.last = this.first;
    this.first = this.first.next;
    this.last.next = null;
  }

  @Override
  public String toString() {
    return "scorer(" + this.weight + ")";
  }

}
