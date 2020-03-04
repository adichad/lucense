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

import org.apache.lucene.index.IndexReader;

/**
 * A query that wraps a filter and simply returns a constant score equal to the
 * query boost for every document in the filter.
 * 
 * 
 * @version $Id: CustomConstantScoreQuery.java 807180 2009-08-24 12:26:43Z
 *          markrmiller $
 */
public class CustomConstantScoreQuery extends CustomQuery {
  private float boost = 0.0f;

  protected final Filter filter;

  @Override
  public float getBoost() {
    return this.boost;
  }

  @Override
  public void setBoost(float b) {
    this.boost = b;
  }

  public CustomConstantScoreQuery(Filter filter) {
    this.filter = filter;
  }

  /** Returns the encapsulated filter */
  public Filter getFilter() {
    return this.filter;
  }

  @Override
  public Query rewrite(IndexReader reader) throws IOException {
    return this;
  }

  @Override
  public void extractTerms(Set terms) {
    // OK to not add any terms when used for MultiSearcher,
    // but may not be OK for highlighting
  }

  protected class ConstantWeight extends Weight {
    private Similarity similarity;

    private float queryNorm;

    private float queryWeight;

    public ConstantWeight(Searcher searcher) {
      this.similarity = getSimilarity(searcher);
    }

    @Override
    public Query getQuery() {
      return CustomConstantScoreQuery.this;
    }

    @Override
    public float getValue() {
      return this.queryWeight;
    }

    @Override
    public float sumOfSquaredWeights() throws IOException {
      return 1f;
    }

    @Override
    public void normalize(float norm) {}

    @Override
    public Scorer scorer(IndexReader reader, boolean scoreDocsInOrder, boolean topScorer) throws IOException {
      return new ConstantScorer(this.similarity, reader, this);
    }

    @Override
    public Explanation explain(IndexReader reader, int doc) throws IOException {

      ConstantScorer cs = new ConstantScorer(this.similarity, reader, this);
      boolean exists = cs.docIdSetIterator.advance(doc) == doc;

      ComplexExplanation result = new ComplexExplanation();

      if (exists) {
        result.setDescription("CustomConstantScoreQuery(" + CustomConstantScoreQuery.this.filter + "), product of:");
        result.setValue(this.queryWeight);
        result.setMatch(Boolean.TRUE);
        result.addDetail(new Explanation(getBoost(), "boost"));
        result.addDetail(new Explanation(this.queryNorm, "queryNorm"));
      } else {
        result.setDescription("CustomConstantScoreQuery(" + CustomConstantScoreQuery.this.filter
            + ") doesn't match id " + doc);
        result.setValue(0);
        result.setMatch(Boolean.FALSE);
      }
      return result;
    }
  }

  protected class ConstantScorer extends CustomScorer {
    final DocIdSetIterator docIdSetIterator;

    final float theScore;

    int doc = -1;

    public ConstantScorer(Similarity similarity, IndexReader reader, Weight w) throws IOException {
      super(similarity);
      this.theScore = w.getQuery().getBoost();
      DocIdSet docIdSet = CustomConstantScoreQuery.this.filter.getDocIdSet(reader);
      if (docIdSet == null) {
        this.docIdSetIterator = DocIdSet.EMPTY_DOCIDSET.iterator();
      } else {
        DocIdSetIterator iter = docIdSet.iterator();
        if (iter == null) {
          this.docIdSetIterator = DocIdSet.EMPTY_DOCIDSET.iterator();
        } else {
          this.docIdSetIterator = iter;
        }
      }
    }

    /** @deprecated use {@link #nextDoc()} instead. */
    @Deprecated
    public boolean next() throws IOException {
      return this.docIdSetIterator.nextDoc() != NO_MORE_DOCS;
    }

    @Override
    public int nextDoc() throws IOException {
      return this.docIdSetIterator.nextDoc();
    }

    @Override
    public int docID() {
      return this.docIdSetIterator.docID();
    }

    @Override
    public float score() throws IOException {
      return this.theScore;
    }

    /** @deprecated use {@link #advance(int)} instead. */
    @Deprecated
    public boolean skipTo(int target) throws IOException {
      return this.docIdSetIterator.advance(target) != NO_MORE_DOCS;
    }

    @Override
    public int advance(int target) throws IOException {
      return this.docIdSetIterator.advance(target);
    }

    public Explanation explain(int doc) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public void matchedTermPositions(Map<String, MatchTermPositions> mtp, Set<String> scoreFields) {

    }
  }

  @Override
  public Weight createWeight(Searcher searcher) {
    return new CustomConstantScoreQuery.ConstantWeight(searcher);
  }

  /** Prints a user-readable version of this query. */
  @Override
  public String toString(String field) {
    return "ConstantScore(" + this.filter.toString() + (getBoost() == 1.0 ? ")" : "^" + getBoost());
  }

  /** Returns true if <code>o</code> is equal to this. */
  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof CustomConstantScoreQuery))
      return false;
    CustomConstantScoreQuery other = (CustomConstantScoreQuery) o;
    return (this.getBoost() == other.getBoost()) && this.filter.equals(other.filter);
  }

  /** Returns a hash code value for this object. */
  @Override
  public int hashCode() {
    // Simple add is OK since no existing filter hashcode has a float component.
    return this.filter.hashCode() + Float.floatToIntBits(getBoost());
  }

}
