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

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermPositions;

/**
 * Expert: A <code>ScorerType</code> for documents matching a <code>Term</code>.
 */
final class CustomTermScorer extends CustomScorer {

  // private static final float[] SIM_NORM_DECODER = Similarity
  // .getNormDecoder();

  private Weight weight;

  private TermDocs termDocs;

  private int doc = -1;

  private final int[] docs = new int[32]; // buffered
                                          // doc
                                          // numbers

  private final int[] freqs = new int[32]; // buffered
                                           // term
                                           // freqs

  private int pointer;

  private int pointerMax;

  private float boostValue;

  private TermPositions termPositions;

  private Term term;

  private int queryPos;

  // private static final int SCORE_CACHE_SIZE = 32;

  /**
   * Construct a <code>CustomTermScorer</code>.
   * 
   * @param weight
   *          The weight of the <code>Term</code> in the query.
   * @param td
   *          An iterator over the documents matching the <code>Term</code>.
   * @param similarity
   *          The </code>Similarity</code> implementation to be used for score
   *          computations.
   * @param queryPos
   *          , Term term
   * @param termPositions
   * @param norms
   *          The field norms of the document fields for the <code>Term</code>.
   */
  CustomTermScorer(Weight weight, TermDocs td, Similarity similarity, String field, TermPositions termPositions,
      int queryPos, Term term) {
    super(similarity);
    this.weight = weight;
    this.termDocs = td;
    this.termPositions = termPositions;
    this.term = term;
    this.queryPos = queryPos;

    this.boostValue = weight.getQuery().getBoost();
  }

  @Override
  public void score(Collector c) throws IOException {
    score(c, Integer.MAX_VALUE, nextDoc());
  }

  // firstDocID is ignored since nextDoc() sets 'doc'
  @Override
  protected boolean score(Collector c, int end, int firstDocID) throws IOException {
    c.setScorer(this);

    while (this.doc < end) { // for docs in window
      c.collect(this.doc); // collect score

      if (++this.pointer >= this.pointerMax) {
        this.pointerMax = this.termDocs.read(this.docs, this.freqs); // refill
                                                                     // buffers
        if (this.pointerMax != 0) {
          this.pointer = 0;
        } else {
          this.termDocs.close(); // close stream
          this.doc = Integer.MAX_VALUE; // set to sentinel value
          return false;
        }
      }
      this.doc = this.docs[this.pointer];
      this.termPositions.next();
    }
    return true;
  }

  // TODO adichad: make payload based decisions above.
  /** @deprecated use {@link #docID()} instead. */
  @Deprecated
  public int doc() {
    return this.doc;
  }

  @Override
  public int docID() {
    return this.doc;
  }

  /**
   * Advances to the next document matching the query. <br>
   * The iterator over the matching documents is buffered using
   * {@link TermDocs#read(int[],int[])}.
   * 
   * @return true iff there is another document matching the query.
   * @deprecated use {@link #nextDoc()} instead.
   */
  @Deprecated
  public boolean next() throws IOException {
    return nextDoc() != NO_MORE_DOCS;
  }

  /**
   * Advances to the next document matching the query. <br>
   * The iterator over the matching documents is buffered using
   * {@link TermDocs#read(int[],int[])}.
   * 
   * @return the document matching the query or -1 if there are no more
   *         documents.
   */
  @Override
  public int nextDoc() throws IOException {

    this.pointer++;
    if (this.pointer >= this.pointerMax) {
      this.pointerMax = this.termDocs.read(this.docs, this.freqs); // refill
                                                                   // buffer
      if (this.pointerMax != 0) {
        this.pointer = 0;
      } else {
        this.termDocs.close(); // close stream
        this.termPositions.close();
        return this.doc = NO_MORE_DOCS;
      }
    }

    this.doc = this.docs[this.pointer];
    return this.doc;
  }

  @Override
  public float score() {
    assert this.doc != -1;
    return this.boostValue;
  }

  /**
   * Skips to the first match beyond the current whose document number is
   * greater than or equal to a given target. <br>
   * The implementation uses {@link TermDocs#skipTo(int)}.
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
   * The implementation uses {@link TermDocs#skipTo(int)}.
   * 
   * @param target
   *          The target document number.
   * @return the matching document or -1 if none exist.
   */
  @Override
  public int advance(int target) throws IOException {
    // first scan in cache
    for (this.pointer++; this.pointer < this.pointerMax; this.pointer++) {
      if (this.docs[this.pointer] >= target) {
        // termPositions.next();
        return this.doc = this.docs[this.pointer];
      }
    }

    // not found in cache, seek underlying stream
    boolean result = this.termDocs.skipTo(target);
    this.termPositions.skipTo(target);
    if (result) {
      this.pointerMax = 1;
      this.pointer = 0;
      this.docs[this.pointer] = this.doc = this.termDocs.doc();
      this.freqs[this.pointer] = this.termDocs.freq();
    } else {
      this.doc = NO_MORE_DOCS;
    }
    return this.doc;
  }

  /**
   * Returns an explanation of the score for a document. <br>
   * When this method is used, the {@link #next()} method and the
   * {@link #score(HitCollector)} method should not be used.
   * 
   * @param doc
   *          The document number for the explanation.
   */
  public Explanation explain(int doc) throws IOException {
    CustomTermQuery query = (CustomTermQuery) this.weight.getQuery();
    Explanation tfExplanation = new Explanation();
    int tf = 0;
    while (this.pointer < this.pointerMax) {
      if (this.docs[this.pointer] == doc)
        tf = this.freqs[this.pointer];
      this.pointer++;
    }
    if (tf == 0) {
      if (this.termDocs.skipTo(doc)) {
        if (this.termDocs.doc() == doc) {
          tf = this.termDocs.freq();
        }
      }
    }
    this.termDocs.close();
    tfExplanation.setValue(getSimilarity().tf(tf));
    tfExplanation.setDescription("tf(termFreq(" + query.getTerm() + ")=" + tf + ")");

    return tfExplanation;
  }

  /** Returns a string representation of this <code>CustomTermScorer</code>. */
  @Override
  public String toString() {
    return "scorer(" + this.weight + ")";
  }

  @Override
  public void matchedTermPositions(Map<String, MatchTermPositions> mtp, Set<String> scoreFields) {
    String field = this.term.field();
    if (scoreFields.contains(field)) {
      MatchTermPositions pos;
      if (!mtp.containsKey(field)) {
        pos = new MatchTermPositions();
        mtp.put(field, pos);
      } else {
        pos = mtp.get(field);
      }
      pos.add(this.queryPos, this.termPositions, this.term);
    }
  }
}
