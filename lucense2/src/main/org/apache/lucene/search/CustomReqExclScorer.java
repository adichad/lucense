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
 * A Scorer for queries with a required subscorer and an excluding (prohibited)
 * sub DocIdSetIterator. <br>
 * This <code>Scorer</code> implements {@link Scorer#skipTo(int)}, and it uses
 * the skipTo() on the given scorers.
 */
class CustomReqExclScorer extends CustomScorer {
  private CustomScorer reqScorer;

  private DocIdSetIterator exclDisi;

  private int doc = -1;

  @Override
  public void matchedTermPositions(Map<String, MatchTermPositions> mtp, Set<String> scoreFields) throws IOException {
    this.reqScorer.matchedTermPositions(mtp, scoreFields);
  }

  /**
   * Construct a <code>ReqExclScorer</code>.
   * 
   * @param reqScorer
   *          The scorer that must match, except where
   * @param exclDisi
   *          indicates exclusion.
   */
  public CustomReqExclScorer(Scorer reqScorer, DocIdSetIterator exclDisi) {
    super(null); // No similarity used.
    this.reqScorer = (CustomScorer) reqScorer;
    this.exclDisi = exclDisi;
  }

  /** @deprecated use {@link #nextDoc()} instead. */
  @Deprecated
  public boolean next() throws IOException {
    return nextDoc() != NO_MORE_DOCS;
  }

  @Override
  public int nextDoc() throws IOException {
    if (this.reqScorer == null) {
      return this.doc;
    }
    this.doc = this.reqScorer.nextDoc();
    if (this.doc == NO_MORE_DOCS) {
      this.reqScorer = null; // exhausted, nothing left
      return this.doc;
    }
    if (this.exclDisi == null) {
      return this.doc;
    }
    return this.doc = toNonExcluded();
  }

  /**
   * Advance to non excluded doc. <br>
   * On entry:
   * <ul>
   * <li>reqScorer != null,
   * <li>exclScorer != null,
   * <li>reqScorer was advanced once via next() or skipTo() and reqScorer.doc()
   * may still be excluded.
   * </ul>
   * Advances reqScorer a non excluded required doc, if any.
   * 
   * @return true iff there is a non excluded required doc.
   */
  private int toNonExcluded() throws IOException {
    int exclDoc = this.exclDisi.docID();
    int reqDoc = this.reqScorer.docID(); // may be excluded
    do {
      if (reqDoc < exclDoc) {
        return reqDoc; // reqScorer advanced to before exclScorer, ie. not
                       // excluded
      } else if (reqDoc > exclDoc) {
        exclDoc = this.exclDisi.advance(reqDoc);
        if (exclDoc == NO_MORE_DOCS) {
          this.exclDisi = null; // exhausted, no more exclusions
          return reqDoc;
        }
        if (exclDoc > reqDoc) {
          return reqDoc; // not excluded
        }
      }
    } while ((reqDoc = this.reqScorer.nextDoc()) != NO_MORE_DOCS);
    this.reqScorer = null; // exhausted, nothing left
    return NO_MORE_DOCS;
  }

  @Override
  public int docID() {
    return this.doc;
  }

  /**
   * Returns the score of the current document matching the query. Initially
   * invalid, until {@link #next()} is called the first time.
   * 
   * @return The score of the required scorer.
   */
  @Override
  public float score() throws IOException {
    return this.reqScorer.score(); // reqScorer may be null when next() or
                                   // skipTo()
    // already return false
  }

  /** @deprecated use {@link #advance(int)} instead. */
  @Deprecated
  public boolean skipTo(int target) throws IOException {
    return advance(target) != NO_MORE_DOCS;
  }

  @Override
  public int advance(int target) throws IOException {
    if (this.reqScorer == null) {
      return this.doc = NO_MORE_DOCS;
    }
    if (this.exclDisi == null) {
      return this.doc = this.reqScorer.advance(target);
    }
    if (this.reqScorer.advance(target) == NO_MORE_DOCS) {
      this.reqScorer = null;
      return this.doc = NO_MORE_DOCS;
    }
    return this.doc = toNonExcluded();
  }

}
