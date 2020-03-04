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
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.util.ToStringUtils;

/**
 * A Query that matches documents containing a term. This may be combined with
 * other terms with a {@link BooleanQuery}.
 */
public class CustomTermQuery extends CustomQuery {
  private float boost = 0.0f;

  @Override
  public float getBoost() {
    return this.boost;
  }

  @Override
  public void setBoost(float b) {
    this.boost = b;
  }

  private Term term;

  private int queryPos;

  private TermDocsFactory termDocsFactory;

  private class CustomTermWeight extends Weight {
    private Similarity similarity;

    private float value;

    private int queryPos;

    private TermDocsFactory termDocsFactory;

    public CustomTermWeight(Searcher searcher, int queryPos, TermDocsFactory termDocsFactory) throws IOException {
      this.value = 0;
      this.queryPos = queryPos;
      this.termDocsFactory = termDocsFactory;
    }

    @Override
    public String toString() {
      return "weight(" + CustomTermQuery.this + ")";
    }

    @Override
    public Query getQuery() {
      return CustomTermQuery.this;
    }

    @Override
    public float getValue() {
      return this.value;
    }

    @Override
    public float sumOfSquaredWeights() {
      return 1f;
    }

    @Override
    public void normalize(float queryNorm) {}

    @Override
    public Scorer scorer(IndexReader reader, boolean scoreDocsInOrder, boolean topScorer) throws IOException {

      TermDocs termDocs;
      if (termDocsFactory == null || termDocsFactory instanceof DefaultTermDocsFactoryBuilder.DefaultTermDocsFactory) {
        termDocs = reader.termDocs(CustomTermQuery.this.term);
      } else {
        termDocs = reader.termPositions(CustomTermQuery.this.term);
      }
      TermPositions termPositions = reader.termPositions(CustomTermQuery.this.term);
      if (termDocs == null)
        return null;

      if (termDocsFactory != null) {
        termDocs = termDocsFactory.wrapTermDocs(termDocs);
        termPositions = (TermPositions) termDocsFactory.wrapTermDocs(termPositions);
      }

      return new CustomTermScorer(this, termDocs, this.similarity, CustomTermQuery.this.term.field(), termPositions,
          this.queryPos, CustomTermQuery.this.term);
    }

    @Override
    public Explanation explain(IndexReader reader, int doc) throws IOException {

      Explanation r = new Explanation();
      r.setDescription("weight(" + getQuery() + " in " + doc + "), weight of field: "
          + CustomTermQuery.this.term.field());
      r.setValue(this.value);
      return r;
    }
  }

  /**
   * Constructs a query for the term <code>t</code>.
   * 
   * @param termDocsFactory
   */
  public CustomTermQuery(Term t, int qpos, TermDocsFactory termDocsFactory) {
    this.term = t;
    this.queryPos = qpos;
    this.termDocsFactory = termDocsFactory;
  }

  /** Returns the term of this query. */
  public Term getTerm() {
    return this.term;
  }

  public int getQueryPos() {
    return this.queryPos;
  }

  @Override
  public Weight createWeight(Searcher searcher) throws IOException {
    return new CustomTermWeight(searcher, this.queryPos, this.termDocsFactory);
  }

  @Override
  public void extractTerms(Set terms) {
    terms.add(getTerm());
  }

  /** Prints a user-readable version of this query. */
  @Override
  public String toString(String field) {
    StringBuffer buffer = new StringBuffer();
    if (!this.term.field().equals(field)) {
      buffer.append(this.term.field());
      buffer.append(":");
    }
    buffer.append(this.term.text());
    buffer.append(ToStringUtils.boost(getBoost()));
    return buffer.toString();
  }

  /** Returns true iff <code>o</code> is equal to this. */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof CustomTermQuery))
      return false;
    CustomTermQuery other = (CustomTermQuery) o;
    return (this.getBoost() == other.getBoost()) && this.term.equals(other.term) && (this.queryPos == other.queryPos);
  }

  /** Returns a hash code value for this object. */
  @Override
  public int hashCode() {
    return Float.floatToIntBits(getBoost()) ^ this.term.hashCode() ^ this.queryPos;
  }

}
